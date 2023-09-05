DC_LOCAL_FILE := docker-compose-local.yml

local-up:
	docker-compose -f $(DC_LOCAL_FILE) up -d

local-down:
	docker-compose -f $(DC_LOCAL_FILE) down

local-rebuild:
	docker-compose -f $(DC_LOCAL_FILE) down
	docker-compose -f $(DC_LOCAL_FILE) pull
	docker-compose -f $(DC_LOCAL_FILE) build --no-cache
	docker-compose -f $(DC_LOCAL_FILE) up -d