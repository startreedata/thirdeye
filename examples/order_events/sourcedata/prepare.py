#
# Copyright 2022 StarTree Inc
#
# Licensed under the StarTree Community License (the "License"); you may not use
# this file except in compliance with the License. You may obtain a copy of the
# License at http://www.startree.ai/legal/startree-community-license
#
# Unless required by applicable law or agreed to in writing, software distributed under the
# License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
# either express or implied.
# See the License for the specific language governing permissions and limitations under
# the License.
#

# Build an order management event database
# place at the root of the file
import os
os.chdir(os.path.dirname(os.path.realpath(__file__)))

#
import pandas as pd
DATA_CSV_FILE_PATH = "./olist_orders_dataset.csv"
raw_df = pd.read_csv(DATA_CSV_FILE_PATH)

# we filter `canceled` and `unavailable` rows because there is no timestamp for those events.
# we filter `approved`/`created` rows because there are only 2/5 rows with this status, and I'm not sure what it means.

clean_df = raw_df[~raw_df["order_status"].isin(['canceled', 'unavailable', 'approved', 'created'])]
clean_df["order_status"].value_counts()

# pass from this state table to an event table.
# use the following equivalence:
## order_purchase_timestamp not Nan: `PLACED` event
## order_approved_at not Nan: `IN_PROGRESS`event
## order_delivered_carrier_date not Nan: `SHIPPING` event
## order_delivered_customer_date not Nan: `DELIVERED` event
# This is close to the [IBM Sterling Store Engagement Tracking Orders schema](https://www.ibm.com/docs/en/store-engagement?topic=summary-tracking-orders)
# build this schema:
# order_id, customer_id, event_type, status,

BASE_COLUMNS = ["order_id", "customer_id"]
TIME_COLUMN_TO_EVENT = {"order_purchase_timestamp": "PLACED",
                        "order_approved_at": "IN_PROGRESS",
                        "order_delivered_carrier_date": "SHIPPING",
                        "order_delivered_customer_date": "DELIVERED",
                        }
EVENT_TYPE_COLUMN = "event_type"
EVENT_TIME_COLUMN = "timestamp"

def generate_events(df, time_column, event_name):
    events = df[df[time_column].notna()][BASE_COLUMNS+[time_column]]
    print("Number of {} events: {}".format(event_name,len(events)))
    events.rename(columns={time_column: EVENT_TIME_COLUMN}, inplace=True)
    events[EVENT_TYPE_COLUMN] = event_name
    return events

event_dfs = []
# make smaller for test and debug
SAMPLE_SIZE = 1_000_000_000
sample_df = clean_df.head(SAMPLE_SIZE)
for k,v in TIME_COLUMN_TO_EVENT.items():
    event_dfs.append(generate_events(sample_df, k, v))

event_df = pd.concat(event_dfs)
event_df[EVENT_TIME_COLUMN] = pd.to_datetime(event_df[EVENT_TIME_COLUMN])
# add 3 years for convenience in TE UI
event_df[EVENT_TIME_COLUMN] = event_df[EVENT_TIME_COLUMN] + pd.offsets.DateOffset(years=3)
# make epoch millis
event_df[EVENT_TIME_COLUMN] = (event_df[EVENT_TIME_COLUMN] - pd.Timestamp("1970-01-01")) // pd.Timedelta("1ms")
event_df.sort_values(EVENT_TIME_COLUMN, inplace=True)
event_df.to_csv("./data.csv", index=False)
print("Move data.csv to the rawdata folder when it's ready.")


