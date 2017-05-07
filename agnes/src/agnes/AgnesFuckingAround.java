package agnes;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import vgrechka.db.DBPile;
import vgrechka.db.Db_stuffKt;
import vgrechka.db.ExecuteAndFormatResultForPrinting;

import java.lang.reflect.Method;

import static vgrechka.Jvm_back_platformKt.getBackPlatform;
import static vgrechka.Shared_jvmKt.clog;

@SuppressWarnings("unused")
public class AgnesFuckingAround {
    public static void main(String... args) throws Exception {
        new AgnesFuckingAround();
    }

    private AgnesFuckingAround() throws Exception {
        getBackPlatform().setSpringctx(new AnnotationConfigApplicationContext(AgnesAppConfig.class));
        DBPile.INSTANCE.executeBunchOfSQLStatementsAndCloseConnection(""
                + "drop table if exists agnes_tits;"
                + "drop table if exists agnes_boobs;"
                + "drop table if exists agnes_girls;"
                + ""
                + "create table `agnes_girls` ("
                + "    `id` integer primary key autoincrement,"
                + "    `name` text not null"
                + ");"
                + ""
                + "create table `agnes_boobs` ("
                + "    `id` integer primary key autoincrement,"
                + "    `location` text not null,"
                + "    `girl_id` bigint not null,"
                + "    foreign key (girl_id) references agnes_girls(id)"
                + ");"
                + ""
                + "create table `agnes_tits` ("
                + "    `id` integer primary key autoincrement,"
                + "    `description` text not null,"
                + "    `boob_id` bigint not null,"
                + "    foreign key (boob_id) references agnes_boobs(id)"
                + ");"
                + "");

        String methodName =
                "fuck_1";

        Method method = getClass().getDeclaredMethod(methodName);
        clog("====================================================");
        clog(method.getName());
        clog("====================================================");
        clog();
        method.invoke(this);
        clog("OK");
    }

    private void fuck_1() {
        AgnesGirlRepository repo = getBackPlatform().getSpringctx().getBean(AgnesGirlRepository.class);
        {
            AgnesGirl girl = new AgnesGirl();
            girl.name = "Mandy the Mutant";
            repo.save(girl);
        }
        {
            AgnesGirl girl = repo.findOne(1L);

            {
                AgnesBoob boob = new AgnesBoob();
                boob.location = "middle";
                boob.girl = girl;
                girl.boobs.add(boob);
                girl = repo.save(girl);
            }

            {
                clog("ID assigned to boob: " + girl.boobs.get(0).id);
                clog("Tits: " + girl.boobs.get(0).tits);
                girl.name = girl.name + " (Modified)";
                {
                    AgnesBoob boob = new AgnesBoob();
                    boob.location = "bottom";
                    boob.girl = girl;
                    girl.boobs.add(boob);
                }
                {
                    girl.boobs.get(0).location = "top";
                    {
                        AgnesTit tit = new AgnesTit();
                        tit.description = "A crazy tit";
                        tit.boob = girl.boobs.get(0);
                        girl.boobs.get(0).tits.add(tit);
                    }
                }
                girl = repo.save(girl);
            }

            {
                AgnesTit tit = new AgnesTit();
                tit.description = "A cunning tit";
                tit.boob = girl.boobs.get(1);
                girl.boobs.get(1).tits.add(tit);
                girl = repo.save(girl);
            }
        }
        dumpShit();
    }

    private void dumpShit() {
        clog("Girls");
        clog("-----");
        clog(new ExecuteAndFormatResultForPrinting()
                .sql("select * from agnes_girls").linePerRow().ignite());
        clog("Boobs");
        clog("-----");
        clog(new ExecuteAndFormatResultForPrinting()
                .sql("select * from agnes_boobs").linePerRow().ignite());
        clog("Tits");
        clog("----");
        clog(new ExecuteAndFormatResultForPrinting()
                .sql("select * from agnes_tits").linePerRow().ignite());
    }
}









