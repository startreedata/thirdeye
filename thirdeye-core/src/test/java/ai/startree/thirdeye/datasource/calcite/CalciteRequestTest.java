package ai.startree.thirdeye.datasource.calcite;

import static ai.startree.thirdeye.util.CalciteUtils.queryToNode;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.datasource.calcite.QueryPredicate.DimensionType;
import ai.startree.thirdeye.detectionpipeline.sql.SqlLanguageTranslator;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.datasource.macro.ThirdEyeSqlParserConfig;
import ai.startree.thirdeye.spi.datasource.macro.ThirdeyeSqlDialect;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import java.text.SimpleDateFormat;
import java.util.List;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.annotations.Test;

public class CalciteRequestTest {

  SqlLanguage sqlLanguage = new PinotSqlLanguage();
  SqlParser.Config sqlParserConfig = SqlLanguageTranslator.translate(sqlLanguage.getSqlParserConfig());
  SqlDialect sqlDialect = SqlLanguageTranslator.translate(sqlLanguage.getSqlDialect());

  @Test
  public void test() throws SqlParseException {
    List<Integer> lol2 = List.of(1, 2);
    List<Integer> subList = lol2.subList(1, lol2.size() - 1);
    SqlNode node = queryToNode(
        "SELECT COUNT(*), COUNT(DISTINCT ol) as ra, AVG(jmt) as lol, ts FROM t.loldata WHERE ts > 123345 and lol='str' group by ra, lol Order by ts LIMIT 1000",
        sqlParserConfig);

    CalciteRequest simpleRequest = new CalciteRequest(
        List.of(QueryProjection.of("SUM", List.of("jmt"))),
        // todo cyril in builder pattern - make sure it is not null at build
        List.of(),
        // todo cyril use builder pattern - if not set, default to empty list - make non nullable in main objct
        null,
        null,
        null,
        "lol",
        // todo mark non nullable - check at build
        "raah",
        // todo  mark non nullable - check at build
        null,
        null,
        List.of(),
        // todo cyril use builder pattern - if not set, default to empty list - make non nullable in main object
        null,
        List.of(),
        // todo cyril use builder pattern - if not set, default to empty list - make non nullable in main objct
        List.of(),
        List.of(),
        // todo cyril use builder pattern - if not set, default to empty list - make non nullable in main objct
        null);

    String query = simpleRequest.getSql(sqlLanguage, new PinotSqlExpressionBuilder());

    CalciteRequest complexRequest = new CalciteRequest(
        List.of(
            QueryProjection.of("SUM", List.of("\"jmt\"")),
            QueryProjection.of(MetricAggFunction.PCT95.name(), List.of("fmt")),
            QueryProjection.of(MetricAggFunction.COUNT.name(), List.of("*"), "DISTINCT"),
            QueryProjection.of(MetricAggFunction.COUNT.name(), List.of("col1"), "DISTINCT"),
            QueryProjection.of(MetricAggFunction.COUNT_DISTINCT.name(), List.of("browser")),
            QueryProjection.of("browser")
        ),
        List.of("unix_millis(datetimeColumn)"),
        Period.days(1),
        "EPOCH",
        "time_epoch",
        "mydb",
        "mytable",
        new Interval(DateTime.now(), DateTime.now().plus(Period.days(10))),
        "time_epoch",
        List.of(QueryPredicate.of(new Predicate("browser", OPER.EQ, "chrome"),
            DimensionType.STRING,
            null)),
        "and country!='US'",
        List.of(QueryProjection.of("time_epoch"), QueryProjection.of("country")),
        List.of(),
        List.of("time_epoch"),
        100L
    );
    String query2 = complexRequest.getSql(sqlLanguage, new PinotSqlExpressionBuilder());

    String lol = "";
  }


  // TODO cyril - should be easy to express:
  //  a timeseries --> with timegrouping
  //  a breakdown on a time interval, with the standard format of the time column (no datetimeconvert)
  //  a percentileFunction --> DONE
  // and also: a distinct operator, a custom limit, a order by, a group by
  // a custom select string
  // a custom where string
  // a custom group by string
  // layer that makes the following decision: which dimension is STRING/NUMERIC/BOOLEAN
  // layer that converts MetricAggFunction to a proper format STRING: is this even possible? --> no because arguments are need
  // query projection can have two types: MetricAggFunction or String??

  private static class PinotSqlLanguage implements SqlLanguage {

    private static final ThirdEyeSqlParserConfig SQL_PARSER_CONFIG = new ThirdEyeSqlParserConfig.Builder()
        .withLex("MYSQL_ANSI")
        .withConformance("BABEL")
        .withParserFactory("SqlBabelParserImpl")
        .build();

    private static final ThirdeyeSqlDialect SQL_DIALECT = new ThirdeyeSqlDialect.Builder()
        .withBaseDialect("AnsiSqlDialect")
        .withIdentifierQuoteString("\"")
        .withIdentifierEscapedQuoteString("")
        .build();

    @Override
    public ThirdEyeSqlParserConfig getSqlParserConfig() {
      return SQL_PARSER_CONFIG;
    }

    @Override
    public ThirdeyeSqlDialect getSqlDialect() {
      return SQL_DIALECT;
    }
  }

  private static class PinotSqlExpressionBuilder implements SqlExpressionBuilder {

    public String getTimeFilterExpression(String column, long minTimeMillisIncluded,
        long maxTimeMillisExcluded) {
      return String.format("%s >= %s AND %s < %s",
          column,
          minTimeMillisIncluded,
          column,
          maxTimeMillisExcluded);
    }

    @Override
    public String getTimeGroupExpression(String timeColumn, String timeColumnFormat,
        Period granularity) {
      return String.format(" DATETIMECONVERT(%s,'%s', '1:MILLISECONDS:EPOCH', '%s') ",
          timeColumn,
          timeColumnFormatToPinotFormat(timeColumnFormat),
          periodToPinotFormat(granularity)
      );
    }

    private String timeColumnFormatToPinotFormat(String timeColumnFormat) {
      switch (timeColumnFormat) {
        case "EPOCH_MILLIS":
        case "1:MILLISECONDS:EPOCH":
          return "1:MILLISECONDS:EPOCH";
        case "EPOCH":
        case "1:SECONDS:EPOCH":
          return "1:SECONDS:EPOCH";
        case "EPOCH_HOURS":
        case "1:HOURS:EPOCH":
          return "1:HOURS:EPOCH";
        default:
          new SimpleDateFormat(timeColumnFormat);
          return String.format("1:DAYS:SIMPLE_DATE_FORMAT:%s", timeColumnFormat);
      }
    }

    private String periodToPinotFormat(final Period period) {
      if (period.getYears() > 0) {
        throw new RuntimeException(String.format(
            "Pinot datasource cannot round to yearly granularity: %s",
            period));
      } else if (period.getMonths() > 0) {
        throw new RuntimeException(String.format(
            "Pinot datasource cannot round to monthly granularity: %s",
            period));
      } else if (period.getWeeks() > 0) {
        throw new RuntimeException(String.format(
            "Pinot datasource cannot round to weekly granularity: %s",
            period));
      } else if (period.getDays() > 0) {
        return String.format("%s:DAYS", period.getDays());
      } else if (period.getHours() > 0) {
        return String.format("%s:HOURS", period.getHours());
      } else if (period.getMinutes() > 0) {
        return String.format("%s:MINUTES", period.getMinutes());
      } else if (period.getSeconds() > 0) {
        return String.format("%s:SECONDS", period.getSeconds());
      } else if (period.getMillis() > 0) {
        return String.format("%s:MILLISECONDS", period.getMillis());
      }
      throw new RuntimeException(String.format("Could not translate Period to Pinot granularity: %s",
          period));
    }

    @Override
    public String getCustomDialectSql(final MetricAggFunction metricAggFunction,
        final List<String> operands,
        final String quantifier) {
      switch (metricAggFunction) {
        case PCT50:
        case PCT90:
        case PCT95:
        case PCT99:
          String aggFunctionString = metricAggFunction.name();
          int percentile = Integer.parseInt(aggFunctionString.substring(3));
          checkArgument(operands.size() == 1,
              "Incorrect number of operands for percentile sql generation. Expected: 1. Got: %s",
              operands.size());
          return new StringBuilder().append("PERCENTILE_TDIGEST")
              .append("(")
              .append(operands.get(0))
              .append(",")
              .append(percentile)
              .append(")")
              .toString();
        default:
          throw new UnsupportedOperationException();
      }
    }
  }
}
