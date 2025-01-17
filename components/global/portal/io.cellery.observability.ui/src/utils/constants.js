/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const Constants = {
    Pattern: {
        DATE_TIME: "DD MMM YYYY, hh:mm A",
        PRECISE_DATE_TIME: "DD MMM YYYY, hh:mm:ss A",
        Query: {
            SECONDS: "second(?:s)?",
            MINUTES: "minute(?:s)?",
            HOURS: "hour(?:s)?",
            DAYS: "day(?:s)?",
            MONTHS: "month(?:s)?",
            YEARS: "year(?:s)?"
        }
    },
    Span: {
        Kind: {
            CLIENT: "CLIENT",
            SERVER: "SERVER",
            PRODUCER: "PRODUCER",
            CONSUMER: "CONSUMER"
        }
    },
    InstanceKind: {
        CELL: "Cell",
        COMPOSITE: "Composite"
    },
    Cell: {
        GATEWAY_NAME_PATTERN: /^gateway$/,
        COMPONENT_NAME_PATTERN: /^(.+)--(.+)$/
    },
    System: {
        ISTIO_MIXER_NAME_PATTERN: /^istio-mixer$/,
        GLOBAL_GATEWAY_NAME_PATTERN: /^global-gateway$/,
        SIDECAR_AUTH_FILTER_OPERATION_NAME_PATTERN: /^async\sext_authz\segress$/
    },
    CelleryType: {
        SYSTEM: "System",
        COMPONENT: "Component"
    },
    Status: {
        Success: "Success",
        Error: "Error",
        Warning: "Warning",
        Unknown: "Unknown"
    },
    Dashboard: {
        ALL_VALUE: "All",
        INBOUND: "Inbound",
        OUTBOUND: "Outbound",
        SIDE_NAV_BAR_WIDTH: 240,
        AUTHORIZATION_EP: "/oauth2/authorize?response_type=code"
    },
    Runtime: {
        LOCAL_RUNTIME_ID: "cellery-default",
        DEFAULT_NAMESPACE: "default"
    }
};

/*
 * Constants that are made up of other constants starts here.
 * These cannot be defined in a single object. Hence these are defined separately.
 */
Constants.Pattern.Query.TIME_UNIT = `${Constants.Pattern.Query.YEARS}|${Constants.Pattern.Query.MONTHS}|`
    + `${Constants.Pattern.Query.DAYS}|${Constants.Pattern.Query.HOURS}|${Constants.Pattern.Query.MINUTES}|`
    + `${Constants.Pattern.Query.SECONDS}`;
Constants.Pattern.Query.TIME = `([0-9]+)\\s*(${Constants.Pattern.Query.TIME_UNIT})`;
Constants.Pattern.Query.RELATIVE_TIME = `^\\s*now\\s*(?:-\\s*(?:${Constants.Pattern.Query.TIME}\\s*)+)?$`;

export default Constants;
