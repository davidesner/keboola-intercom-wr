#Intercom Writer

Intercom writer component for Keboola Connection.

##Functionality

The component allows updating Intercom User model using Intercom’s Bulk API.

You can create or update large number of user. If correspondent user is not found in the destination database, new user will be created. Intercom allows specifying custom attributes for both User and Company object same principle applies for them – if a custom attribute is not found in the object, new attribute will be created. This is important to keep in mind when configuring the component, because at this time, no validation of custom columns input is implemented. This is due to the limitations of Intercom API. 

Intercom asynchronous bulk API allows submitting large volume of User data at the same time. Although the submitting itself is fast, it takes some time before they get processed. Lately, Intercom is experiencing some issues with bulk job queues and the processing time may exceed the component’s run-time limitation. For this reason, it may happen that the results of submitted jobs won’t be collected within the single run. The job results which did not get processed in time will be collected on the next run of the component. 

Records which failed to be updated will be stored in output table `user_errors.csv` along with the error message and timestamp of the failed job.

##Configuration

###Parameters

 - **App ID** – *(REQ)* your Intercom application ID
 - **API Key** – *(REQ)* Intercom API key.
 - **User column mapping** – *(REQ)* Specify column name mapping of your source table to match Intercom user attributes.
	 - **Standard User attributes** – specify column name mapping to match the standard Intercom User attributes. If standard attributes mapping is not specified, app will expect to find default attribute names in the input. Note that `user_id` attribute is required.
	 - **Custom User attributes** –  specify mapping for User custom attributes. All custom attributes must be specified in order to update them. ***NOTE*:** Be careful when defining the attribute names, they must match exactly the attribute names in your Intercom model, if not, new custom attribute will be created!
	 - **Company attributes mapping** – *(OPT)* Standard and Custom attributes of Intercom Company model. Each user is allowed to belong to one company. Same rules as for User mapping applies. Required standard attribute is `company_id`.

##Input
Input table with Intercom User data.

##Output
`default_bucket.user_error.csv` containing records which failed to update.

##Sample configurations / use cases

###Use case 1
