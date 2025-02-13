---
id: authorization
title: Authorization
sidebar_label: Authorization
---

To simplify management process and reduce complex permission system between users and available projects,
Reposilite uses path based token system.

## Access token
The access token consists of four elements:

* Path - the path covered by the token
* Alias - the short form associated with token
* Permissions - the permissions associated with token
* Token - generated secret token used to access associated path

Currently supported permissions:

* `w` - allows to write *(deploy)* using this token
* `m` - marks token as manager token *(full access)*

As an example, we can imagine we have several projects located in our repository. 
As an administrator, we want to have permission to whole repository, so the credentials for us should look like this:

```properties
path: /
alias: admin
permissions: m
```

Access to requested paths is resolved by comparing the access token path with the beginning of current uri. Our `admin` user associated with `/` has access to all paths, because all of requests starts with this path separator:

| URI | Status |
| :-- | :----: |
| / | Ok |
|/releases | Ok |
|/releases/artifactId/groupId | Ok |
|/snapshots | Ok |

We also might add some of co-workers to their projects. 
For instance, we will add *Daenerys Targaryen* user as `khaleesi` to `com.hbo.got` project:

```properties
path: /releases/com/hbo/got
alias: khaleesi
```

The access table for the following configuration:

| URI | Status |
| :-- | :----: |
| / | Unauthorized |
| /releases/com/hbo/got | Ok |
| /releases/com/hbo/got/sub-project | Ok |

We recommend to use user-specific names for individual access tokens.
In case of a larger teams, 
it might be a good idea to use project name as an alias and distribute shared access token between them:

```properties
# for a crew
alias: got
# or mixed solution to increase traffic control
alias: got_khaleesi
```

Finally, we can also grant access to multiple repositories using `*` wildcard operator.
As you can see, we have to provide the repository name in access token path. 
In a various situations, we want to maintain `releases` and `snapshots` repositories for the same project.
Instead of generating separate access token, we can just replace repository name with wildcard operator:

```properties
path: */com/hbo/got
alias: khaleesi
```

### Generate token
Tokens are generated using the `keygen` command in Reposilite CLI:

```log
$ keygen <path> <alias> <permissions>
```

As an example, we can generate access token for our `root` user:

```bash
$ keygen / root m
19:55:20.692 INFO | Generated new access token for root (/) with 'm' permissions
19:55:20.692 INFO | AW7-kaXSSXTRVL_Ip9v7ruIiqe56gh96o1XdSrqZCyTX2vUsrZU3roVOfF-YYF-y
19:55:20.723 INFO | Stored tokens: 1
```

### List tokens
To display list of all generated tokens, just use `tokens` command in Reposilite CLI:

```bash
$ tokens
23:48:57.880 INFO | Tokens (2)
23:48:57.880 INFO | /releases/auth/test as authtest
23:48:57.880 INFO | / as admin
```

### Revoke tokens
You can revoke token using the `revoke <alias>` command in Reposilite CLI.

## Deploy
The deploy phase adds your artifact to a remote repository automatically.
Before we get to that, you have to make sure that the deploy feature is enabled in Repository:

```properties
# Accept deployment connections
deployEnabled: true
```

Now, you should determine in your `pom.xml` the target repository where Maven should upload artifact.
Let's say we want to deploy artifact to the `releases` repository:

```xml
<distributionManagement>
    <repository>
        <id>local-repository</id>
        <url>http://localhost:80/releases</url>
    </repository>
</distributionManagement>
```

To use generated token add, a new server in your [~/m2/settings.xml](https://maven.apache.org/settings.html):

```xml
<server>
  <!-- Id has to match the id provided in pom.xml -->
  <id>local-repository</id>
  <username>{alias}</username>
  <password>{token}</password>
</server>
```

If you've configured everything correctly, you should be able to deploy artifact using the following command:

```bash
$ mvn deploy
```