CREATE TABLE "devices"(
    "device_id" BIGINT NOT NULL,
    "brand" BIGINT NOT NULL,
    "model" BIGINT NOT NULL
);
ALTER TABLE
    "devices" ADD PRIMARY KEY("device_id");
CREATE TABLE "subject_sets"(
    "subject_id" TEXT NOT NULL,
    "image_id_1" TEXT NULL,
    "image_id_2" TEXT NULL,
    "image_id_3" TEXT NULL
);
ALTER TABLE
    "subject_sets" ADD PRIMARY KEY("subject_id");
CREATE TABLE "animal_species"(
    "animal_common_name_code" TEXT NOT NULL,
    "animal_subspecies_name" TEXT NULL,
    "animal_species_name" TEXT NOT NULL,
    "animal_genus_name" TEXT NOT NULL,
    "animal_family_name" TEXT NOT NULL,
    "animal_order_name" TEXT NOT NULL
);
ALTER TABLE
    "animal_species" ADD PRIMARY KEY("animal_common_name_code");
CREATE TABLE "classifications"(
    "subject_id" TEXT NOT NULL,
    "animal_common_name_code" TEXT NOT NULL
);
CREATE TABLE "capture_events"(
    "capture_event_id" TEXT NOT NULL,
    "interval_threshold" TEXT NOT NULL
);
ALTER TABLE
    "capture_events" ADD PRIMARY KEY("capture_event_id");
CREATE TABLE "sd_cards"(
    "sd_card_id" TEXT NOT NULL,
    "model" TEXT NOT NULL,
    "transfer_rate" TEXT NULL,
    "memory_capacity" TEXT NULL
);
ALTER TABLE
    "sd_cards" ADD PRIMARY KEY("sd_card_id");
CREATE TABLE "project_deployments"(
    "project_id" TEXT NOT NULL,
    "deployment_id" TEXT NOT NULL
);
ALTER TABLE
    "project_deployments" ADD PRIMARY KEY("project_id");
CREATE TABLE "image_animals"(
    "image_id" TEXT NOT NULL,
    "animal_species_id" TEXT NOT NULL,
    "count" INTEGER NOT NULL
);
ALTER TABLE
    "image_animals" ADD PRIMARY KEY("image_id");
CREATE TABLE "images"(
    "image_id" TEXT NOT NULL,
    "capture_event_id" TEXT NULL,
    "camera_id" TEXT NULL,
    "sd_card_id" TEXT NULL,
    "deployment_id" TEXT NULL,
    "timestamp" TIMESTAMP(0) WITH
        TIME zone NOT NULL
);
ALTER TABLE
    "images" ADD PRIMARY KEY("image_id");
CREATE TABLE "deployments"(
    "deployment_id" TEXT NOT NULL,
    "deployer_names" TEXT NULL,
    "latitude" DECIMAL(8, 2) NULL,
    "longitude" DECIMAL(8, 2) NULL,
    "site" TEXT NOT NULL,
    "protocol" TEXT NULL,
    "deployment_date" TEXT NOT NULL,
    "deployment_time" TIMESTAMP(0) WITH
        TIME zone NOT NULL,
        "collection_date" TEXT NOT NULL,
        "collection_time" TIMESTAMP(0)
    WITH
        TIME zone NOT NULL,
        "last_use_date" TEXT NULL,
        "device_type" TEXT NOT NULL,
        "device_id" TEXT NOT NULL,
        "sd_card_id" TEXT NOT NULL,
        "media_type" TEXT NOT NULL,
        "battery_percent_start" DECIMAL(8, 2) NULL,
        "battery_percent_end" DECIMAL(8, 2) NULL,
        "device_worked_at_collection" BOOLEAN NULL,
        "tree_species_id" TEXT NULL,
        "deployment_notes" TEXT NULL,
        "flag" TEXT NULL,
        "flag_notes" TEXT NULL
);
ALTER TABLE
    "deployments" ADD PRIMARY KEY("deployment_id");
COMMENT
ON COLUMN
    "deployments"."last_use_date" IS 'The date when the camera stopped taking "effective" pictures. For example, if bear knocks camera before collection, the effective number of trap nights is less than deployment - collection.';
CREATE TABLE "classification_events"(
    "subject_id" BIGINT NOT NULL,
    "activity" TEXT NULL,
    "behavior" TEXT NOT NULL,
    "snow" TEXT NULL,
    "species_dependent_traits" TEXT NULL
);
ALTER TABLE
    "classification_events" ADD PRIMARY KEY("subject_id");
CREATE TABLE "projects"(
    "project_id" TEXT NOT NULL,
    "project_name" TEXT NOT NULL,
    "description" TEXT NULL,
    "objectives" TEXT NULL,
    "start_date" TEXT NULL,
    "end_date" TEXT NULL
);
ALTER TABLE
    "projects" ADD PRIMARY KEY("project_id");
CREATE TABLE "tree_species"(
    "tree_species_id" TEXT NOT NULL,
    "tree_species_name" TEXT NOT NULL,
    "tree_genus_name" TEXT NOT NULL,
    "tree_family_name" TEXT NOT NULL,
    "tree_common_name" TEXT NOT NULL
);
ALTER TABLE
    "tree_species" ADD PRIMARY KEY("tree_species_id");
CREATE TABLE "protocols"(
    "protocol_id" TEXT NOT NULL,
    "camera_setup" TEXT NOT NULL,
    "details" TEXT NULL
);
ALTER TABLE
    "protocols" ADD PRIMARY KEY("protocol_id");
ALTER TABLE
    "deployments" ADD CONSTRAINT "deployments_device_id_foreign" FOREIGN KEY("device_id") REFERENCES "devices"("device_id");
ALTER TABLE
    "project_deployments" ADD CONSTRAINT "project_deployments_deployment_id_foreign" FOREIGN KEY("deployment_id") REFERENCES "deployments"("deployment_id");
ALTER TABLE
    "subject_sets" ADD CONSTRAINT "subject_sets_image_id_3_foreign" FOREIGN KEY("image_id_3") REFERENCES "images"("image_id");
ALTER TABLE
    "subject_sets" ADD CONSTRAINT "subject_sets_image_id_2_foreign" FOREIGN KEY("image_id_2") REFERENCES "images"("image_id");
ALTER TABLE
    "project_deployments" ADD CONSTRAINT "project_deployments_project_id_foreign" FOREIGN KEY("project_id") REFERENCES "projects"("project_id");
ALTER TABLE
    "classifications" ADD CONSTRAINT "classifications_animal_common_name_code_foreign" FOREIGN KEY("animal_common_name_code") REFERENCES "animal_species"("animal_common_name_code");
ALTER TABLE
    "subject_sets" ADD CONSTRAINT "subject_sets_image_id_1_foreign" FOREIGN KEY("image_id_1") REFERENCES "images"("image_id");
ALTER TABLE
    "deployments" ADD CONSTRAINT "deployments_sd_card_id_foreign" FOREIGN KEY("sd_card_id") REFERENCES "sd_cards"("sd_card_id");
ALTER TABLE
    "deployments" ADD CONSTRAINT "deployments_tree_species_id_foreign" FOREIGN KEY("tree_species_id") REFERENCES "tree_species"("tree_species_id");
ALTER TABLE
    "deployments" ADD CONSTRAINT "deployments_protocol_id_foreign" FOREIGN KEY("protocol_id") REFERENCES "protocols"("protocol_id");
ALTER TABLE
    "images" ADD CONSTRAINT "images_sd_card_id_foreign" FOREIGN KEY("sd_card_id") REFERENCES "sd_cards"("sd_card_id");