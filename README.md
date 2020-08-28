# Snowflake to STDOUT

Snowflake to STDOUT (in JSON).

## Why

I needed something that will stream results to SDOUT in JSON

## What it supports:

- streaming from the JDBC result set
- streaming from the internal Snowflake stage

## Required

- SNOWFLAKE_USER 
- SNOWFLAKE_PASSWORD 
- SNOWFLAKE_URL //, eg: "jdbc:snowflake://account-name.eu-west-1.snowflakecomputing.com/"

create a Snowflake Internal stage beforehand

```
create or replace stage test_stage
  file_format = (type = 'JSON' );
```

## How

- unload to internal snowflake stage in JSON format
- stream to stdout

## How to 

java -jar ...\
    --use-result-set \
    --sql "SELECT object_construct(*)::varchar FROM big_table order by updated_at" 

Unload, stream, but don't delete the files:

java -jar ...\
    --stage test_stage \
    --prefix "foo/bar1/" \
    --sql "SELECT object_construct(*) FROM big_table order_by updated_at" 
    --keep

Stream existing json files, don't delete

java -jar ...\
    --stage test_stage \
    --prefix "foo/bar1/"
    --keep

## Extra info

- `object_construct` - https://docs.snowflake.com/en/sql-reference/functions/object_construct.html