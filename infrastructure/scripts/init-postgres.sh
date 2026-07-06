#!/bin/bash
set -e

echo "Creating multiple databases and users..."

psql -v ON_ERROR_STOP=1 --username "root" --dbname "postgres" <<-EOSQL
    CREATE USER userservice WITH PASSWORD 'userpassword123';
    CREATE DATABASE userdb;
    GRANT ALL PRIVILEGES ON DATABASE userdb TO userservice;

    CREATE USER paymentservice WITH PASSWORD 'paymentpassword123';
    CREATE DATABASE paymentdb;
    GRANT ALL PRIVILEGES ON DATABASE paymentdb TO paymentservice;

    CREATE USER orderservice WITH PASSWORD 'orderpassword123';
    CREATE DATABASE orderdb;
    GRANT ALL PRIVILEGES ON DATABASE orderdb TO orderservice;

    CREATE USER restaurantservice WITH PASSWORD 'restaurantpassword123';
    CREATE DATABASE restaurantdb;
    GRANT ALL PRIVILEGES ON DATABASE restaurantdb TO restaurantservice;

    CREATE USER deliveryservice WITH PASSWORD 'deliverypassword123';
    CREATE DATABASE deliverydb;
    GRANT ALL PRIVILEGES ON DATABASE deliverydb TO deliveryservice;

    CREATE USER notificationservice WITH PASSWORD 'notificationpassword123';
    CREATE DATABASE notificationdb;
    GRANT ALL PRIVILEGES ON DATABASE notificationdb TO notificationservice;
EOSQL

echo "Granting PostgreSQL 15+ schema permissions..."
psql -v ON_ERROR_STOP=1 --username "root" --dbname "userdb" -c "GRANT ALL ON SCHEMA public TO userservice;"
psql -v ON_ERROR_STOP=1 --username "root" --dbname "paymentdb" -c "GRANT ALL ON SCHEMA public TO paymentservice;"
psql -v ON_ERROR_STOP=1 --username "root" --dbname "orderdb" -c "GRANT ALL ON SCHEMA public TO orderservice;"
psql -v ON_ERROR_STOP=1 --username "root" --dbname "restaurantdb" -c "GRANT ALL ON SCHEMA public TO restaurantservice;"
psql -v ON_ERROR_STOP=1 --username "root" --dbname "deliverydb" -c "GRANT ALL ON SCHEMA public TO deliveryservice;"
psql -v ON_ERROR_STOP=1 --username "root" --dbname "notificationdb" -c "GRANT ALL ON SCHEMA public TO notificationservice;"

echo "Databases and users successfully initialized."
