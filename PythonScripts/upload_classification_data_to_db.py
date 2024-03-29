"""
Makes connections and sends SQL queries to a database.
"""
import argparse
import boto3
import functools
import json
from io import StringIO
import os
import random
import re
import string
import time
from typing import Any, Dict, List, Mapping, Optional, Sequence, Union

import numpy as np
import pandas as pd
from psycopg2.errors import OperationalError, ReadOnlySqlTransaction
from psycopg2.extensions import connection
from psycopg2.extras import DictCursor, NamedTupleCursor
from psycopg2.pool import SimpleConnectionPool


_POOL: Dict[str, Any] = {}
_PID = os.getpid()
_REGION_NAME = "us-east-2"
_PARAMETER_NAME = (
    "arn:aws:ssm:us-east-2:590184055410:parameter/north-country-wild-credentials"
)


def return_connection(environment: str, connection: connection) -> None:
    pool = _POOL[environment]
    pool.putconn(connection)


def get_dsn(config: Dict[str, str]) -> str:
    return "user='{user}' password='{password}' dbname='{dbname}' host='{host}'".format(
        **config
    )


# _POOL is just a dictionary with environment as the key and the SimpleConnectionPool
# as the value
def _get_connection_from_config(environment: str, config: Dict[str, str]) -> connection:
    global _PID, _POOL
    if _PID != os.getpid():
        # multi-processing, we need to flush the pools
        _POOL = {}
        _PID = os.getpid()
    try:
        return _POOL.setdefault(
            environment, SimpleConnectionPool(1, 20, get_dsn(config))
        ).getconn()
    except OperationalError as e:
        print(f"OperationalError occured while getting connection pool. {e}")
        time.sleep(10)
        if _PID != os.getpid():
            # re-flush the pool in case of multiprocessing
            _POOL = {}
            _PID = os.getpid()
        return _POOL.setdefault(
            environment, SimpleConnectionPool(1, 20, get_dsn(config))
        ).getconn()


def get_connection(environment: str) -> connection:
    parameter_json = boto3.client(
        "ssm", region_name=_REGION_NAME, endpoint_url=None
    ).get_parameter(Name=_PARAMETER_NAME)["Parameter"]["Value"]
    config = json.loads(parameter_json)
    return _get_connection_from_config(environment, config)


def execute(
    ENVIRONMENT: str,
    sql: str,
    substitutions: Optional[Union[Sequence, Mapping]] = None,
    connection: Optional[connection] = None,
) -> None:
    """
    For SQL statements with no return value, like INSERT
    """
    local_connection = get_connection(ENVIRONMENT) if connection is None else connection
    cursor = local_connection.cursor()
    cursor.execute(sql, substitutions)
    cursor.close()
    local_connection.commit()
    if local_connection != connection:
        return_connection(ENVIRONMENT, local_connection)


def get_list(
    ENVIRONMENT: str,
    sql: str,
    substitutions: Optional[Union[Sequence, Mapping]] = None,
    connection: Optional[connection] = None,
) -> List[Any]:
    rows = run_query(
        ENVIRONMENT=ENVIRONMENT,
        sql=sql,
        substitutions=substitutions,
        connection=connection,
    )
    return [row[0] for row in rows]


def _validate_col_names(df: pd.DataFrame, table_name: str) -> None:
    environment = "north_country_wild"
    table_columns = get_list(
        environment,
        f"SELECT column_name FROM information_schema.columns where table_name = '{table_name}';",
    )
    # Column names in Postgresql are case-insensitive unless surrounded by quotes. Default to all-lowercase
    if sorted(df.columns.str.lower().to_list()) != sorted(table_columns):
        raise Exception("Columns in new dataframe do not match columns in table")


def _make_temp_table_name():
    random_str = "".join([random.choice(string.ascii_lowercase) for i in range(10)])
    return f"temp_bulk_upload_{random_str}"


def _create_upsert_conflict_statement(pkeys: List[str], columns: List[str]) -> str:
    """
    ON CONFLICT (pkeys)
    DO UPDATE SET
        col1 = EXCLUDED.col1,
        col2 = EXCLUDED.col2,
        ...
    """
    conflict_str = ", ".join(pkeys)
    on_conflict = f"ON CONFLICT ({conflict_str}) DO NOTHING"
    return f"{on_conflict}"


def _create_upsert_statement(
    table_name: str, temp_table_name: str, pkeys: List[str], columns: List[str]
) -> str:
    insert = f"INSERT INTO {table_name} SELECT * FROM {temp_table_name}"
    conflict_statement = _create_upsert_conflict_statement(pkeys, columns)
    return f"{insert} {conflict_statement}"


def _bulk_insert_df_with_conn(
    df: pd.DataFrame, table_name: str, conn: connection, use_custom_na_rep: bool = False
) -> None:
    # http://initd.org/psycopg/docs/usage.html#copy
    filelike = StringIO()
    if use_custom_na_rep:
        df.to_csv(filelike, index=False, header=False, na_rep="\\N")
    else:
        df.to_csv(filelike, index=False, header=False)

    cursor = conn.cursor()
    try:
        filelike.seek(0)
        cursor.copy_from(filelike, table_name, sep=",")
        conn.commit()
    finally:
        cursor.close()


def upsert_from_df(
    df: pd.DataFrame, table_name: str, pkeys: List[str], use_custom_na_rep: bool = False
):
    _validate_col_names(df, table_name)

    env = "north_country_wild"
    connection = get_connection(env)

    temp_table_name = _make_temp_table_name()
    upsert_statement = _create_upsert_statement(
        table_name, temp_table_name, pkeys, df.columns
    )

    exc = functools.partial(execute, env, connection=connection)

    try:
        exc(f"DROP TABLE IF EXISTS {temp_table_name}")
        exc(f"CREATE TEMPORARY TABLE {temp_table_name} (LIKE {table_name});")
        _bulk_insert_df_with_conn(df, temp_table_name, connection, use_custom_na_rep)
        exc(upsert_statement)
    finally:
        return_connection(env, connection)


def bulk_insert_with_copy_expert(df, table_name):
    validate_table_name(table_name)

    environment = "north_country_wild"
    filelike = StringIO()
    df.to_csv(filelike, index=False, header=False)

    conn = get_connection(environment)
    cursor = conn.cursor()
    try:
        filelike.seek(0)
        cursor.copy_expert(f"COPY {table_name} FROM stdin (format csv)", filelike)
        conn.commit()
    finally:
        cursor.close()
    return_connection(environment, conn)


def upload_data_from_csv(
    csv_file: str,
) -> None:
    classifications_df = pd.read_csv(csv_file)
    classifications_df = classifications_df.replace({np.nan: None})
    print(classifications_df.head())
    subject_sets_df = classifications_df[
        ["subject_id", "image_id_1", "image_id_2", "image_id_3"]
    ]
    simple_classifications_df = classifications_df[["subject_id", "common_name_code"]]
    upsert_from_df(subject_sets_df, "subject_sets", pkeys=["subject_id"])
    upsert_from_df(
        simple_classifications_df,
        "classifications",
        pkeys=["subject_id", "common_name_code"],
    )
    print("Successfully uploaded data!")


def run_query(
    ENVIRONMENT: str,
    sql: str,
    substitutions: Optional[Union[Sequence, Mapping]] = None,
    as_dictionary: bool = False,
    connection: Optional[connection] = None,
    read_only: bool = False,
) -> List[Any]:
    """
    Returns results as a list of named tuples using the column names, eg
    [Record(id=2, request_id='44b4b11e-8e5b-11e7-ac3a-0b3b907cea19', lifecycle_hook_name=None, acknowledged_at=None)]
    or dictionaries if as_dictionary is True
    """
    cursor_factory = DictCursor if as_dictionary else NamedTupleCursor
    real_connection = get_connection(ENVIRONMENT) if connection is None else connection
    if read_only:
        real_connection.set_session(readonly=True)
    cursor = real_connection.cursor(cursor_factory=cursor_factory)
    try:
        cursor.execute(sql, substitutions)
    except ReadOnlySqlTransaction as e:
        # Important to close the cursor/end any transaction before re-raising the error
        # Otherwise later update/deletes etc. can run into issues due to locked data.
        cursor.close()
        real_connection.close()
        raise (e)
    except OperationalError as e:
        print("nope")
        raise (e)
    rows = cursor.fetchall()
    cursor.close()
    real_connection.commit()
    if connection is None:
        return_connection(ENVIRONMENT, real_connection)
    return rows


def validate_table_name(table_name):
    # We're open to SQL injection in some places where we string-template queries :(
    if not re.match(r"^[\w]+$", table_name):
        raise RuntimeError(f"Nice try; {table_name} not a safe SQL table name")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="""
        This script takes a test csv with at least five columns (potentially more):\n
        subject_id, image_id_1, image_id_2, and image_id_3, and common_name_code and\n
        and uploads the data into two separate tables:\n
        subject_sets and classifications. This is cleaned-up data that you would expect\n
        to see after concatenating multiple classification IDs into one "true" ID.
        """
    )
    parser.add_argument("--classifications-csv", type=str, required=True)

    args = parser.parse_args()
    print("Brett needs to add a check for proper header names in file")
    upload_data_from_csv(args.classifications_csv)


# execute(
#     "north_country_wild",
#     "INSERT INTO cameras (camera_id, model) VALUES('C09', 'bushnell 2290')",
# )
