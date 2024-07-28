# JPA Pitfalls and Solutions

#### Table of Contents
1. [Introduction](#introduction)
2. [N+1 Query Problem](#n1-query-problem)
    - [One-to-Many Relationship](#one-to-many-relationship)
        - [Method: `showNPlusOneProblemOneToMany`](#method-shownplusoneproblemonetomany)
        - [Method: `solveNPlusOneWithSubSelectOneToMany`](#method-solvenplusonewithsubselectonetomany)
        - [Method: `showSubSelectForIndividualArticlesOneToMany`](#method-showsubselectforindividualarticlesonetomany)
        - [Method: `solveNPlusOneWithEntityGraph`](#method-solvenplusonewithentitygraph)
    - [Many-to-One Relationship](#many-to-one-relationship)
        - [Method: `showNPlusOneProblemManyToOne`](#method-shownplusoneproblemmanytoone)
        - [Method: `solveNPlusOneManyToOne`](#method-solvenplusonemanytoone)
3. [Unnecessary Dirty Checking in Non-ReadOnly Transactions](#unnecessary-dirty-checking)
    - [Method: `showProblemWithNonReadOnlyTransaction`](#method-showproblemwithnonreadonlytransaction)
    - [Method: `showSolutionWithReadOnlyTransaction`](#method-showsolutionwithreadonlytransaction)
4. [Context Doesn't Refresh Automatically After Update Query](#context-doesnt-refresh)
    - [Method: `showNoAutomaticRefreshAfterUpdate`](#method-shownoautomaticrefreshafterupdate)
    - [Method: `showNoAutomaticRefreshAfterSingleEntityUpdate`](#method-shownoautomaticrefreshaftersingleentityupdate)
    - [Method: `showManualRefreshAfterUpdate`](#method-showmanualrefreshafterupdate)
    - [Method: `showManualRefreshAfterSingleEntityUpdate`](#method-showmanualrefreshaftersingleentityupdate)
5. [LazyInitializationException Problem](#lazyinitializationexception-problem)
    - [Method: `showLazyInitializationException`](#method-showlazyinitializationexception)
    - [Method: `showSolutionToLazyInitializationException`](#method-showsolutiontolazyinitializationexception)

---

# Introduction

This repository demonstrates common pitfalls encountered when using Java Persistence API (JPA) with Hibernate and provides solutions to mitigate them. Each section below describes a specific problem, its context, and examples where the issue manifests in code.

---

# N+1 Query Problem

[NPlusOne.java](/src/test/java/dev/roman/jpapitfalls/repository/NPlusOne.java)

The N+1 query problem occurs when a query fetches data that includes relationships to other entities, and then for each fetched entity, an additional query is executed to fetch its related data. This can lead to a significant increase in the number of database queries, impacting performance.

## One-to-Many Relationship

### Method: `showNPlusOneProblemOneToMany`

In this method, for each article fetched, additional queries are executed to fetch its associated comments individually.

### Method: `solveNPlusOneWithSubSelectOneToMany`

This method demonstrates using a subselect to fetch comments for all articles in a single query, thereby avoiding the N+1 query problem.

### Method: `showSubSelectForIndividualArticlesOneToMany`

Here, a subselect is used to fetch comments for specific articles, optimizing query execution.

### Method: `solveNPlusOneWithEntityGraph`

Using `@EntityGraph`, this method fetches articles with their comments in a single query, efficiently resolving the N+1 query problem.

---

## Many-to-One Relationship

### Method: `showNPlusOneProblemManyToOne`

This method highlights the N+1 problem in a many-to-one relationship context, where each comment is fetched separately for its associated article.

### Method: `solveNPlusOneManyToOne`

Here, comments are fetched with their associated articles in a single query, resolving the N+1 issue efficiently.

---

# Unnecessary Dirty Checking in Non-ReadOnly Transactions

[UnnecessaryDirtyCheckingInNonReadOnlyTransaction.java](/src/test/java/dev/roman/jpapitfalls/repository/UnnecessaryDirtyCheckingInNonReadOnlyTransaction.java)

When using non-read-only transactions in Hibernate, unnecessary dirty checking may occur after each transaction, impacting performance, especially when the transaction is intended solely for reading data.

### Method: `showProblemWithNonReadOnlyTransaction`

In this method, transactions are executed with `readOnly = false`, enabling dirty checking. This is unnecessary when the transaction only reads data, potentially degrading performance.

### Method: `showSolutionWithReadOnlyTransaction`

Here, transactions are set to `readOnly = true`, disabling dirty checking. This approach enhances performance by skipping unnecessary dirty checks when data is only read.

---

# Context Doesn't Refresh Automatically After Update Query

[ContextDoesntRefreshAutomaticallyAfterUpdateQuery.java](/src/test/java/dev/roman/jpapitfalls/repository/ContextDoesntRefreshAutomaticallyAfterUpdateQuery.java)

After executing an update query in JPA, the persistence context does not automatically refresh its state to reflect the changes made in the database. This can lead to inconsistencies when accessing entities post-update.

### Method: `showNoAutomaticRefreshAfterUpdate`

This test demonstrates that after updating multiple articles' names in the database, the old names still persist in the persistence context unless manually refreshed.

### Method: `showNoAutomaticRefreshAfterSingleEntityUpdate`

Here, after updating a single article's name in the database, the old name remains in the persistence context without automatic refresh.

### Method: `showManualRefreshAfterUpdate`

This test shows how to manually refresh the persistence context after updating multiple articles' names, ensuring that the updated names are correctly reflected.

### Method: `showManualRefreshAfterSingleEntityUpdate`

Similarly, this method demonstrates manually refreshing the persistence context after updating a single article's name to reflect the updated data accurately.

---

These tests illustrate the importance of understanding how the persistence context behaves after update operations in JPA. By manually refreshing the context where necessary, developers can ensure consistency between database updates and entity states.

---

# LazyInitializationException Problem<a name="lazyinitializationexception-problem"></a>

[LazyInitializationExceptionProblem.java](/src/test/java/dev/roman/jpapitfalls/repository/LazyInitializationExceptionProblem.java)

LazyInitializationException occurs in JPA when attempting to access uninitialized proxies or collections outside of an active session context.

### Method: `showLazyInitializationException`<a name="method-showlazyinitializationexception"></a>

This test method demonstrates the occurrence of LazyInitializationException when accessing uninitialized comments of an article outside of a transactional context.

### Method: `showSolutionToLazyInitializationException`<a name="method-showsolutiontolazyinitializationexception"></a>

Here, the test method shows the correct approach to avoid LazyInitializationException by accessing comments within an active transactional context.

---

These tests illustrate the importance of managing entity initialization and transactional boundaries to avoid LazyInitializationException in JPA applications.