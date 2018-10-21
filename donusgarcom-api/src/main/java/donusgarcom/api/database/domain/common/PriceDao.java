package donusgarcom.api.database.domain.common;

import donusgarcom.api.database.domain.CoreDao;

public class PriceDao extends CoreDao {
    @Override
    public SqlField[] getManagedSqlFields() {
        return new SqlField[] {
                new SqlField("decimal", SqlFieldType.INT),
                new SqlField("floating", SqlFieldType.INT)
        };
    }

    public static class Price {
        public int decimal;
        public int floating;

        public Price() {

        }

        public Price(int decimal, int floating) {
            this.decimal = decimal;
            this.floating = floating;
        }

        public void setDecimal(int decimal) {
            this.decimal = decimal;
        }

        public void setFloating(int floating) {
            this.floating = floating;
        }

        public int getDecimal() {
            return this.decimal;
        }

        public int getFloating() {
            return this.floating;
        }
    }
}
