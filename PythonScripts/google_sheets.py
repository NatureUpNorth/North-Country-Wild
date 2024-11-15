from io import BytesIO

import requests
from requests.sessions import Session
from urllib3.util import Retry


class RetryWithHighMax(Retry):
    BACKOFF_MAX = 1000


def _request_session_with_retry() -> Session:
    session = requests.Session()
    adapter = requests.adapters.HTTPAdapter()
    adapter.max_retries = RetryWithHighMax(
        total=3,
        backoff_factor=10,
        status_forcelist=[500, 501, 502, 503],
    )
    session.mount("http://", adapter)
    session.mount("https://", adapter)
    return session


def get_gsheet_as_byteslike(url: str) -> BytesIO:
    session = _request_session_with_retry()
    resp = session.get(url)
    return BytesIO(resp.content)
