/**
 * This code was generated by
 * \ / _    _  _|   _  _
 *  | (_)\/(_)(_|\/| |(/_  v1.0.0
 *       /       /
 */
package com.twilio.rest.api.v2010.account.usage.record;


public class AmplAllTimeTest {
    @mockit.Mocked
    private com.twilio.http.TwilioRestClient twilioRestClient;

    @org.junit.Before
    public void setUp() throws java.lang.Exception {
        com.twilio.Twilio.init("AC123", "AUTH TOKEN");
    }

    @org.junit.Test
    public void testReadRequest() {
        new mockit.NonStrictExpectations() {
            {
                com.twilio.http.Request request = new com.twilio.http.Request(com.twilio.http.HttpMethod.GET, com.twilio.rest.Domains.API.toString(), "/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/AllTime.json");
                twilioRestClient.request(request);
                times = 1;
                result = new com.twilio.http.Response("", 500);
                twilioRestClient.getAccountSid();
                result = "AC123";
            }
        };
        try {
            com.twilio.rest.api.v2010.account.usage.record.AllTime.reader("ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").read();
            org.junit.Assert.fail("Expected TwilioException to be thrown for 500");
        } catch (com.twilio.exception.TwilioException e) {
        }
    }

    @org.junit.Test
    public void testReadFullResponse() {
        new mockit.NonStrictExpectations() {
            {
                twilioRestClient.request(((com.twilio.http.Request) (any)));
                result = new com.twilio.http.Response("{\"end\": 0,\"first_page_uri\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/AllTime?Page=0&PageSize=1\",\"last_page_uri\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/AllTime?Page=68&PageSize=1\",\"next_page_uri\": null,\"num_pages\": 69,\"page\": 0,\"page_size\": 1,\"previous_page_uri\": null,\"start\": 0,\"total\": 69,\"uri\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/AllTime\",\"usage_records\": [{\"account_sid\": \"ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"api_version\": \"2010-04-01\",\"category\": \"sms-inbound-shortcode\",\"count\": \"0\",\"count_unit\": \"messages\",\"description\": \"Short Code Inbound SMS\",\"end_date\": \"2015-09-04\",\"price\": \"0\",\"price_unit\": \"usd\",\"start_date\": \"2011-08-23\",\"subresource_uris\": {\"all_time\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/AllTime.json?Category=sms-inbound-shortcode\",\"daily\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/Daily.json?Category=sms-inbound-shortcode\",\"last_month\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/LastMonth.json?Category=sms-inbound-shortcode\",\"monthly\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/Monthly.json?Category=sms-inbound-shortcode\",\"this_month\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/ThisMonth.json?Category=sms-inbound-shortcode\",\"today\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/Today.json?Category=sms-inbound-shortcode\",\"yearly\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/Yearly.json?Category=sms-inbound-shortcode\",\"yesterday\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/Yesterday.json?Category=sms-inbound-shortcode\"},\"uri\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/AllTime?Category=sms-inbound-shortcode&StartDate=2011-08-23&EndDate=2015-09-04\",\"usage\": \"0\",\"usage_unit\": \"messages\"}]}", com.twilio.http.TwilioRestClient.HTTP_STATUS_CODE_OK);
                twilioRestClient.getObjectMapper();
                result = new com.fasterxml.jackson.databind.ObjectMapper();
            }
        };
        org.junit.Assert.assertNotNull(com.twilio.rest.api.v2010.account.usage.record.AllTime.reader("ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").read());
    }

    @org.junit.Test
    public void testReadEmptyResponse() {
        new mockit.NonStrictExpectations() {
            {
                twilioRestClient.request(((com.twilio.http.Request) (any)));
                result = new com.twilio.http.Response("{\"end\": 0,\"first_page_uri\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/AllTime?Page=0&PageSize=1\",\"last_page_uri\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/AllTime?Page=68&PageSize=1\",\"next_page_uri\": null,\"num_pages\": 69,\"page\": 0,\"page_size\": 1,\"previous_page_uri\": null,\"start\": 0,\"total\": 69,\"uri\": \"/2010-04-01/Accounts/ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/Usage/Records/AllTime\",\"usage_records\": []}", com.twilio.http.TwilioRestClient.HTTP_STATUS_CODE_OK);
                twilioRestClient.getObjectMapper();
                result = new com.fasterxml.jackson.databind.ObjectMapper();
            }
        };
        org.junit.Assert.assertNotNull(com.twilio.rest.api.v2010.account.usage.record.AllTime.reader("ACaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").read());
    }
}

