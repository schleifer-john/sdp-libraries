.. _Artifactory Library: 
---------
Artifactory
---------


Jfrog Artifactory is a Universal Repository manager that allows organizations to securely store and retrieve artifacts. It provides access control of those artifacts, and offers High-Availability, multiple push replications, and tags library with searchable metadata.

Steps
=====
* download(pattern, target, flat)
* upload()

Please refer to the official Jfrog Jenkins Artifactory documentation on downloading and uploading files for more information and examples: https://jfrog.com/help/r/jfrog-integrations-documentation/uploading-and-downloading-files

download(pattern, target, flat)
==========
This command is used to download files from Artifactory

.. csv-table:: Download Arguments
   :header: "Argument", "Description"

   "pattern", "The pattern of files to download from Artifactory."
   "target", "The target local directory where the files will be downloaded to."
   "flat", "If true, artifacts are downloaded to the exact target path specified and their hierarchy in the source repository is ignored. If false, artifacts are downloaded to the target path in the file system while maintaining their hierarchy in the source repository."

Example Usage Snippet

Downloads the `sample/artifact.zip` file in the `SAMPLE-REPO` in Artifactory to a local folder named `libraries`.
The flat argument is set to `true`, so the downloaded file path will be `libraries/artifact.zip`.  If set to `false`, the path will be `libraries/sample/artifact.zip` instead.

.. code:: groovy

   download("SAMPLE-REPO/sample/artifact.zip", "libraries", true)


Library Configuration Options
=============================


.. csv-table::  Artifactory Library Configuration Options
   :header: "Field", "Description", "Default Value"

   "creds_id", "A new credential can be used", "artifactory-creds"
   "url", "Specifies the URL that tenant will be used to access Artifactory", "https://artifactory.helix.gsa.gov/artifactory"


Example Configuration Snippet
=============================

.. code:: groovy

    artifactory {
        url = "https://artifactory.helix.gsa.gov/artifactory"
        creds_id = "artifactory-creds"
    }

Artifactory Configurations
============================



External Dependencies
=====================

* Artifactory server must be up, and operational. 
* Jenkins must have a credential to access Artifactory, this is done by default when using the deployment script.
* Artifactory URL must be configured in ``Manage Jenkins > Configure System``
===
