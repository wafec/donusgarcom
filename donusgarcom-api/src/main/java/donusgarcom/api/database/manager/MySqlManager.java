package donusgarcom.api.database.manager;

import donusgarcom.api.database.core.SqlManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MySqlManager extends SqlManager {
    static final Logger log = LogManager.getLogger(MySqlManager.class);

    public MySqlManager() {
        super("com.mysql.jdbc.Driver",
                "jdbc:mysql://wallace-laptop/donusgarcomdb",
                "thegarcom",
                "loveIsTheKey");
    }


}
