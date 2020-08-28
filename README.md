# Snowflake to STDOUT

Snowflake to STDOUT (in JSON).

## Why

I needed something that will stream results to SDOUT in JSON

## What it supports

- streaming from the JDBC result set
- streaming from the internal named stage

## Required

- SNOWFLAKE_USER 
- SNOWFLAKE_PASSWORD 
- SNOWFLAKE_URL //, eg: "jdbc:snowflake://account-name.eu-west-1.snowflakecomputing.com/"

create a Snowflake Internal stage beforehand

```
create or replace stage test_stage
  file_format = (type = 'JSON' );
```

https://docs.snowflake.com/en/user-guide/data-unload-snowflake.html

## How to ?

### Simple

Stream results of the basic query:

```bash
./target/jars/snowflake-to-stdout \
  --sql "SELECT object_construct(*)::varchar FROM big_table order by updated_at" 
```


### Use internal snowflake stages

You need to create one before! 

copy to stage and stream:

```bash
./target/jars/snowflake-to-stdout \
    --stage test_stage \
    --prefix "foo/bar1/" \
    --sql "SELECT object_construct(*) FROM big_table order_by updated_at" 
```

Don't delete files from the stage 

```bash
./target/jars/snowflake-to-stdout \
    --stage test_stage \
    --prefix "foo/bar1/" \
    --sql "SELECT object_construct(*) FROM big_table order_by updated_at" 
    --keep
```

Stream existing json files, don't delete

```bash
./target/jars/snowflake-to-stdout \
    --stage test_stage \
    --prefix "foo/bar1/" \
    --keep
```

## Extra info

- `object_construct` - https://docs.snowflake.com/en/sql-reference/functions/object_construct.html