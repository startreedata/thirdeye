const config =
    // eslint-disable-next-line max-len
    "---\nname: newAlert\ndescription: This is the detection used by online service\nnodes:\n  d1:\n    type: DETECTION\n    subType: PERCENTAGE_RULE\n    metric:\n      name: views\n      dataset:\n        name: pageviews\n    params:\n      offset: wo1w\n      percentageChange: 0.2\n";

export default config;
