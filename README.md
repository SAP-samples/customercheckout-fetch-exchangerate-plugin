# SAP Customer Checkout FetchExchangeRate Plugin
<!-- Please include descriptive title -->

<!--- Register repository https://api.reuse.software/register, then add REUSE badge:
[![REUSE status](https://api.reuse.software/badge/github.com/SAP-samples/REPO-NAME)](https://api.reuse.software/info/github.com/SAP-samples/REPO-NAME)
-->

## Description
Sample code for a plugin to fetch live exchange rates integrated via the SAP Customer Checkout plugin framework.

## Project Structure
The plugin works out of the box with the provided example `Exchange Rate API`. Credits <a href="https://www.exchangerate-api.com">Rates By Exchange Rate API</a>.

This sample project is structured in a way to allow for quick expansion of the available APIs.
To add a new API `Foo`:

1. Add a new `FooApiClient` by extending `RetrofitClient` and declaring the endpoints as well as the response `DTO` (data transfer object) of the service
2. Add a new  `FooApiCommunicator` by extending `AbstractCommunicator` and implementing the HTTP(s) communication logic that is necessary for your API
3. Add a new `FooApi` by extending `AbstractExchangeAPI` and implementing the `exchangeRateInRelationToUSD` method.
4. Extend the `APIFactory` with a new `public static AbstractExchangeAPI fooAPI()` method.

Finally, you can select the API in the `FetchExchangeRatePlugin::startup` method.

## Requirements
Working instance of the SAP Customer Checkout Point of Sales software.

## Download and Installation
Set the path to your ENV.jar, then build via maven `mvn clean install`.

## Known Issues
No known issues.

## How to obtain support
[Create an issue](https://github.com/SAP-samples/customercheckout-fetchexchangerate-plugin/issues) in this repository if you find a bug or have questions about the content.
 
For additional support, [ask a question in SAP Community](https://answers.sap.com/questions/ask.html).

## Contributing
If you wish to contribute code, offer fixes or improvements, please send a pull request. Due to legal reasons, contributors will be asked to accept a DCO when they create the first pull request to this project. This happens in an automated fashion during the submission process. SAP uses [the standard DCO text of the Linux Foundation](https://developercertificate.org/).

## License
Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved. This project is licensed under the Apache Software License, version 2.0 except as noted otherwise in the [LICENSE](LICENSE) file.