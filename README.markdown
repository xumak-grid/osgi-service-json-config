# osgi-service-config

A configuration servlet for Adobe AEM 6.0+ used for Grid configuration provisioning.


## How to build
 - Copy the settings.xml file in your .m2 directory

 Resources/settings.xml

 - Build without SonarQube

 mvn clean install -PautoInstallBundle -Pkas-pass

 - Build with Local Analysis

 mvn clean install -PautoInstallBundle

 - Build & Analysis Report to SonarQube

 mvn clean install -PautoInstallBundle -Pkas-qaqc

## SonarQube url

 - http://10.4.4.237:9000/

## USAGE

Do a POST Request to a path ``` /system/instanceConfiguration.servlet.html``` of your AEM  instance

- Curl command:

```bash
curl -u <USER>:<PASS> \
  -H 'Content-Type:application/json' \
  -H 'Accept: application/json' \
  --data-binary @<MY-FILE.json> \
  -X POST http://<HOST>:<PORT>/system/instanceConfiguration.servlet.html
```

### Options available 

1. OSGi Configurations
    * Update or Create
 
2. Users
    * Create
    * Update
    * Change password
 
3. Replication Agents
    * Create
    * Update
    * Show
    * Delete


JSON Request Examples:

- OSGi

  * Update or Create

```json
{
  "prodAuthor01": {
    "configs": [
      {
        "policy": "UPDATE_CREATE_POLICY",
        "type": "osgi",
        "id": "com.xumak.jcrsyncr.engine.SyncControllerImpl6.config",
        "path": "/apps/system/config",
        "with": {
          "jcrsync.definitions": [
            "/BedrocK/XCQB/Demo/Source/myCompany/CQFiles/myCompany;/apps/grid"
          ],
          "jcrsync.establish.sync": true,
          "foo": "bar",
          "number": 123
        }
      }
    ]
  }
}
```

- User

  * Create a user

```json
{
  "prodAuthor01": {
    "users": [
      {
        "type": "internal",
        "id": "author",
        "password": "LQSc8zGIFToaZbDpZmQVGelaNr7e",
        "policy": "CREATE",
        "with": {
          "profile/aboutMe": "Used the Replication Agents to send everything under /content/*"
        },
        "acls": {
          "deny": {
            "/": [
              "C",
              "R",
              "U",
              "D",
              "X",
              "R*",
              "U*"
            ]
          },
          "allow": {
            "/content": [
              "R"
            ]
          }
        }
      }
    ]
  }
}
```

  * Update a user

```json
{
  "prodAuthor01": {
    "users": [
      {
        "type": "internal",
        "id": "author",
        "policy": "UPDATE",
        "with": {
          "profile/aboutMe": "Used the Replication Agents to send everything under /content/*"
        },
        "acls": {
          "deny": {
            "/": [
              "C",
              "R",
              "U",
              "D",
              "X",
              "R*",
              "U*"
            ]
          },
          "allow": {
            "/content": [
              "R"
            ]
          }
        }
      }
    ]
  }
}
```

  * Change password

```json
{
  "prodAuthor01": {
    "users": [
      {
        "policy": "CHANGE_PASSWORD",
        "type": "root",
        "id": "abc",
        "password": "12345",
        "newPassword": "admin"
      }
    ]
  }
}

```

- Replication Agents

  * Create, Update, Delete, Show

```json
{
  "prodAuthor01": {
    "replicationAgents": [
      {
        "type": "publish",
        "name": "toProdPublish1",
        "policy": "CREATE",
        "with": {
          "jcr:title": "Replication Agent for ProdPublish1",
          "enabled": true,
          "userId": "allContentSender",
          "host": "prod.publish1.customer.xumak.cloud",
          "port": 5501,
          "transportUser": "allContentReceiver",
          "transportPassword": "zzhV4gaDuxF9HUcdDaJhajjA",
          "currentVersion": "832-040312048-1pou4poiup4oi123u4pio13up4"
        }
      },
      {
        "type": "publish",
        "name": "toProdPublish1",
        "policy": "UPDATE",
        "with": {
          "foo": "bar"
        }
      },
      {
        "type": "publish",
        "name": "toProdPublish1",
        "policy": "SHOW"
      },
      {
        "type": "publish",
        "name": "toProdPublish1",
        "policy": "DELETE"
      },
      {
        "type": "publish",
        "policy": "SHOW"
      }
    ]
  }
}
```

Copyright © 2016 Tikal Technologies, Inc.