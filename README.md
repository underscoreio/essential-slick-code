# Source Code to Accompany _Essential Slick_.

## About the Book

_Essential Slick_ provides a compact, no-nonsense guide to everything you need to know to use Slick in a commercial setting:

*    Chapter 1 provides an abbreviated overview of the library as a whole, demonstrating the fundamentals of data modelling, connecting to the database, and running queries.
*    Chapter 2 covers basic select queries, introducing Slick’s query language and delving into some of the details of type inference and type checking.
*    Chapter 3 covers queries for inserting, updating, and deleting data.
*    Chapter 4 looks at action combinators.
*    Chapter 5 discusses data modelling, including defining custom column and table types.
*    Chapter 6 explores advanced select queries, including joins and aggregates.
*    Chapter 7 provides a brief overview of Plain SQL queries. This is a useful tool when you need fine control over the SQL sent to your database.

To find out more about the book and download the preview chapters, see [Underscore.io](https://underscore.io/books/essential-slick/).

  _If you're looking for the example code for Slick 2.1, use the [2.1 branch](https://github.com/underscoreio/essential-slick-code/tree/2.1)_.

## About the Code

The code is organised as a folder for each chapter. Each folder contains an SBT project.

Each file is either the examples from the book, or the scaffolding for the exercises.


### Chapter 1 (Basics), 2 (Selecting Data), 3 (Creating and Modifying Data), and 4 (Action Combinators)

These projects contain a single source file, _main.scala_. Use the SBT `run` or `~run` command.

For example:

```
$ cd chapter-01
$ sbt
...
> run
...
[info] Running Example
Creating database table

Inserting test data

Selecting all messages:
Message(Dave,Hello, HAL. Do you read me, HAL?,1)
Message(HAL,Affirmative, Dave. I read you.,2)
Message(Dave,Open the pod bay doors, HAL.,3)
Message(HAL,I'm sorry, Dave. I'm afraid I can't do that.,4)

Selecting only messages from HAL:
Message(HAL,Affirmative, Dave. I read you.,2)
Message(HAL,I'm sorry, Dave. I'm afraid I can't do that.,4)
[success] Total time: 5 s, completed 06/05/2015 2:22:22 PM
```

### Chapter 5 (Data Modelling)

Chapter 5 contains several applications. Using the SBT `run` command will prompt you for the file to run.

Alternatively, use `runMain` or `~runMail` and supply the name of the class to run a particular example:

```
$ cd chapter-05
$ sbt
> ~runMain StructureExample
```

The examples are:

1. `StructureExample` in _structure.scala_ - an illustration of separating schema and profile.
2. `HListExampleApp` in _hlists.scala_ - the HList example from the book.
3. `NestedCaseClassExampleApp` in _nested_case_class.scala_ - is the exercise on custom case class mapping.
4. `NullExample` in _nulls.scala_ - where the `User` table has an optional `email` field.
5. `PKExample` in _primary_keys.scala_ - the `User.id` becomes an `Option[Long]`, and the `OccupantTable` is added.
6. `ForeignKeyExample` in _foreign_keys.scala_ - where `MessageTable` has a foreign key to the `UserTable`.
7. `ValueClassesExample` in _value_classes.scala_ - introduces types for primary keys, as `MessagePK` and `UserPk`.
8. `SumTypesExample` in _sum_types.scala_ - the message `Flag`ing example from the book.
9. `CustomBooleanExample` in _custom_boolean.scala_ - is the "Custom Boolean" exercise code.

### Chapter 6 (Joins and Aggregates)

This project contains a _chat_schema.scala_ file that defines the schema for the chapter.
It also defines the method `populate` which inserts our standard cast, rooms, and messages into the database.

The schema is re-used in the following examples:

1. `JoinsExample` in _joins.scala_ - runs through a variety of joins using the sample data in _chat_schema.scala_.
2. `AggregatesExample` in _aggregates.scala_ - various group by and aggregation examples..

_joins.scala_ contains much that is commented out.  Remove the comments from around the code you are interest in to run it.


### Chapter 7 (Plain SQL)

This project contains the following examples:

1. `SelectExample` in _select.scala_ - gives examples with the `sql` interpolator.
2. `UpdateExample` in _updates.scala_ - gives examples with the `sqlu` interpolator.
3. `TsqlExample` in _tsql.scala_ - examples using typed plain queries.

