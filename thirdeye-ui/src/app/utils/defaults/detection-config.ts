const config =
    // eslint-disable-next-line max-len
    "---\n- name: pc5\n  description: Percentage drop\n  detections:\n    detection_rule_1:\n      type: PERCENTAGE_RULE\n      metric:\n        name: views\n        dataset:\n          name: pageviews\n      params:\n        offset: wo1w\n        percentageChange: 0.1\n        pattern: down\n  lastTimestamp: 0\n";

export default config;
