# address-reputation-store

[![Build Status](https://travis-ci.org/hmrc/address-reputation-store.svg?branch=master)](https://travis-ci.org/hmrc/address-reputation-store) [ ![Download](https://api.bintray.com/packages/hmrc/releases/address-reputation-store/images/download.svg) ](https://bintray.com/hmrc/releases/address-reputation-store/_latestVersion)

This provides an abstraction for the MongoDB access that is used by both
[address-reputation-ingester](https://github.com/hmrc/address-reputation-ingester) and
its dependent [address-lookup](https://github.com/hmrc/address-lookup) microservice.

### Test Data

When running through service manager some test data is preloaded. You can see this test data under [conf/data](https://github.com/HMRC/address-lookup/tree/master/conf/data), for example these [test addresses](https://github.com/HMRC/address-lookup/blob/master/conf/data/testaddresses.csv).

This test data is also pre-loaded in the Dev, QA and Staging instances of the service.

### Licence

This code is open source software licensed under the 
[Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
    
