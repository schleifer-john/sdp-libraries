.. _Artifactory Library: 
---------
Artifactory
---------


Jfrog Artifactory is a Universal Repository manager that allows organizations to securely store and retrieve artifacts. It provides access control of those artifacts, and offers High-Availability, multiple push replications, and tags library with searchable metadata.

Steps Contributed
=================
* download()
* upload()


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