import { Drawer, makeStyles } from '@material-ui/core';
import React, { ReactElement } from 'react';
import DisplayFilter from './filter/display-filter.component';
import FilterDropdownComponent from './filter/filter-dropdown.component';
import QuickFilters from './filter/quick-filters.component';

const drawerWidth = 360;
const useStyles = makeStyles(() => {return {
    root: {
      display: 'flex',
    },
    drawer: {
      width: drawerWidth,
      flexShrink: 0,
    },
    drawerPaper: {
      width: drawerWidth,
    }, 
  }});

// Just a demonstraight component
const SideBar = (): ReactElement => {
    const classes = useStyles();

    return <Drawer anchor="left"
                className={classes.drawer}
                classes={{
                    paper: classes.drawerPaper,
                }}
                variant="permanent"
            >
                <QuickFilters
                    filters={[
                        {name: "alerts i subscribe to (0)", value: 'subscribed'}, 
                        {name: "alerts i own (0)", value: 'own'}, 
                        {name: "all alerts (1)", value: 'all'}]} 
                    onFilter={(value): void => { console.log(value)} } 
                />
                <DisplayFilter />
                <FilterDropdownComponent label="Applications" labelProp="name"  placeholder="Select Applications"  valueProp="id" />
                <FilterDropdownComponent label="Subscription Groups" labelProp="name" placeholder="Select Subscription Groups"  valueProp="id" />
                <FilterDropdownComponent label="Owners" labelProp="name"  placeholder="Select Owners"  valueProp="id" />
                <FilterDropdownComponent label="Detection Type" labelProp="name" placeholder="Select Detection Type"  valueProp="id" />
                <FilterDropdownComponent label="Metrics" labelProp="name"  placeholder="Select Metrics"  valueProp="id" />
            </Drawer>
}

export default SideBar;