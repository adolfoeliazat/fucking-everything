package agnes;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import vgrechka.BigPile;
import vgrechka.db.BaseSQLiteAppConfig;

@EnableJpaRepositories
@ComponentScan(basePackages = {"agnes"})
class AgnesAppConfig extends BaseSQLiteAppConfig {
    public AgnesAppConfig() {
        super(new String[] {"agnes"});
    }

    @NotNull
    @Override
    protected String getDatabaseURL() {
        return BigPile.INSTANCE.getLocalSQLiteShebangTestDBURL();
    }
}
