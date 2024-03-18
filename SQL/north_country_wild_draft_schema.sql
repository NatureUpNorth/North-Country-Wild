CREATE TABLE projects (
        project_id text PRIMARY KEY,
        project_name text NOT NULL,
        description text,
        objectives text,
        start_date text,
        end_date text
);

CREATE TABLE deployments (
        deployment_id text PRIMARY KEY,
        project_id text NOT NULL,
        start_date text,
        end_date text,
        camera_id text NOT NULL,
        sd_card_id text NOT NULL,
        battery_percent_start numeric,
        battery_percent_end numeric,
        latitude numeric,
        longitude numeric,
        tree_species_id text
);

ALTER TABLE ONLY public.deployments
    ADD CONSTRAINT deployments_fkey FOREIGN KEY (project_id) REFERENCES public.projects(project_id);

CREATE TABLE project_deployments (
        project_id text PRIMARY KEY,
        deployment_id text NOT NULL,
        protocol_id text
);

ALTER TABLE ONLY public.project_deployments
    ADD CONSTRAINT project_deployments_fkey FOREIGN KEY (project_id) REFERENCES public.projects(project_id);

ALTER TABLE ONLY public.project_deployments
    ADD CONSTRAINT project_deployments_fkey FOREIGN KEY (deployment_id) REFERENCES public.deployments(deployment_id);


CREATE TABLE project_deployments (
        project_id text,
        deployment_id text NOT NULL,
        protocol_id text
);

CREATE TABLE images (
        image_id text PRIMARY KEY,
        capture_event_id text,
        camera_id text,
        sd_card_id text,
        deployment_id text,
        timestamp timestamp with time zone NOT NULL,
        protocol_id text
);

CREATE TABLE image_animals (
        image_id text PRIMARY KEY,
        animal_species_id text NOT NULL,
        count integer NOT NULL
);

--Make camera_id a certain number of characters?
CREATE TABLE cameras (
        camera_id text PRIMARY KEY,
        model text NOT NULL
);

CREATE TABLE sd_cards (
        sd_card_id text PRIMARY KEY,
        model text NOT NULL,
        transfer_rate text,
        memory_capacity text
);

-- How does this connect to other tabes?
CREATE TABLE classification_events (
        activity text PRIMARY KEY,
        behavior text NOT NULL,
        snow text,
        species_dependent_traits text
);

CREATE TABLE capture_events (
        capture_event_id text PRIMARY KEY,
        interval_threshold text NOT NULL
);

CREATE TABLE tree_species (
        tree_species_id text PRIMARY KEY,
        tree_species_name text NOT NULL,
        tree_genus_name text NOT NULL,
        tree_family_name text NOT NULL,
        tree_common_name text NOT NULL
);

CREATE TABLE animal_species (
        animal_species_id text PRIMARY KEY,
        animal_species_name text NOT NULL,
        animal_genus_name text NOT NULL,
        animal_family_name text NOT NULL,
        animal_order_name text NOT NULL,
        animal_class_name text NOT NULL,
        animal_phylum_name text NOT NULL,
        animal_kingdom_name text NOT NULL,
        animal_common_name text NOT NULL
);

CREATE TABLE protocols (
        protocol_id text PRIMARY KEY,
        camera_setup text NOT NULL,
        details text
);