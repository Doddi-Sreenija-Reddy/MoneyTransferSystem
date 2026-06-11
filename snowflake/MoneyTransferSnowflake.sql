CREATE WAREHOUSE IF NOT EXISTS COMPUTE_WH 
  WAREHOUSE_SIZE = 'XSMALL' 
  AUTO_SUSPEND = 60 
  AUTO_RESUME = TRUE;

  CREATE DATABASE IF NOT EXISTS MONEY_TRANSFER_DW;
  CREATE SCHEMA IF NOT EXISTS MONEY_TRANSFER_DW.ANALYTICS;
  use database MONEY_TRANSFER_DW;
  use warehouse compute_wh;
  use schema analytics;

  CREATE OR REPLACE STAGE STG_MONEY_TRANSFER 
  FILE_FORMAT = (TYPE = CSV FIELD_OPTIONALLY_ENCLOSED_BY = '"' SKIP_HEADER = 1);

--Dimension table
CREATE OR REPLACE TABLE DIM_ACCOUNT (
    account_key    NUMBER AUTOINCREMENT,
    account_id     STRING, -- Maps to MySQL 'id'
    holder_name    STRING,
    status         STRING,
    effective_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    PRIMARY KEY (account_key)
);

-- Dimension: Date
CREATE OR REPLACE TABLE DIM_DATE (
    date_key  NUMBER PRIMARY KEY,
    full_date DATE,
    day       NUMBER,
    month     NUMBER,
    year      NUMBER,
    quarter   NUMBER
);

-- Fact: Transactions (Matches your 8 columns in MySQL)
CREATE OR REPLACE TABLE FACT_TRANSACTIONS (
    transaction_key  NUMBER AUTOINCREMENT,
    account_from_key NUMBER,
    account_to_key   NUMBER,
    date_key         NUMBER,
    amount           NUMBER(18,2),
    status           STRING,
    CONSTRAINT FK_FROM FOREIGN KEY (account_from_key) REFERENCES DIM_ACCOUNT(account_key),
    CONSTRAINT FK_TO FOREIGN KEY (account_to_key) REFERENCES DIM_ACCOUNT(account_key),
    CONSTRAINT FK_DATE FOREIGN KEY (date_key) REFERENCES DIM_DATE(date_key)
);

LIST @STG_MONEY_TRANSFER;

COPY INTO DIM_ACCOUNT (account_id, holder_name, status)
FROM (
    SELECT t.$1, t.$3, t.$6 
    FROM @STG_MONEY_TRANSFER/accounts.csv t
);

-- Populate Date Dimension
INSERT INTO DIM_DATE (date_key, full_date, day, month, year, quarter)
SELECT 
    TO_NUMBER(TO_CHAR(d, 'YYYYMMDD')), d, DAY(d), MONTH(d), YEAR(d), QUARTER(d)
FROM (SELECT DATEADD(DAY, SEQ4(), '2026-01-01') AS d FROM TABLE(GENERATOR(ROWCOUNT => 365)));

INSERT INTO FACT_TRANSACTIONS (account_from_key, account_to_key, date_key, amount, status)
SELECT 
    da_from.account_key, 
    da_to.account_key, 
    TO_NUMBER(TO_CHAR(CAST(t.$3 AS DATE), 'YYYYMMDD')), -- created_on is $3
    t.$2, -- amount is $2
    t.$7  -- status is $7
FROM @STG_MONEY_TRANSFER/transaction_logs.csv t
JOIN DIM_ACCOUNT da_from ON da_from.account_id = t.$5 -- from_account_id is $5
JOIN DIM_ACCOUNT da_to   ON da_to.account_id   = t.$8; -- to_account_id is $8

--ANALYTICS--

-- 1. Daily Transaction Volume
SELECT d.full_date, SUM(f.amount) as total_amount
FROM FACT_TRANSACTIONS f
JOIN DIM_DATE d ON f.date_key = d.date_key
GROUP BY d.full_date;

-- 2. Success Rate (Pie Chart)
SELECT status, COUNT(*) as tx_count
FROM FACT_TRANSACTIONS
GROUP BY status;

-- 3. Average Transfer Amount
SELECT AVG(amount) FROM FACT_TRANSACTIONS WHERE status = 'SUCCESS';

--peak hours
-- X-Axis: HOUR_OF_DAY, Y-Axis: TX_COUNT
SELECT 
    HOUR(CAST(t.$3 AS TIMESTAMP)) AS hour_of_day, 
    COUNT(*) AS tx_count
FROM @STG_MONEY_TRANSFER/transaction_logs.csv t
GROUP BY hour_of_day
ORDER BY hour_of_day;

--active users
SELECT 
    a.holder_name, 
    COUNT(f.transaction_key) AS total_transactions,
    SUM(f.amount) AS total_volume
FROM FACT_TRANSACTIONS f
JOIN DIM_ACCOUNT a ON f.account_from_key = a.account_key
GROUP BY a.holder_name
ORDER BY total_transactions DESC