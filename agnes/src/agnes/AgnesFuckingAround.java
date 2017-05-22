package agnes;

import kotlin.jvm.functions.Function1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.TransactionStatus;
import vgrechka.db.DBPile;
import vgrechka.db.Db_stuffKt;
import vgrechka.db.ExecuteAndFormatResultForPrinting;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

import static vgrechka.Jvm_back_platformKt.getBackPlatform;
import static vgrechka.Shared_jvmKt.clog;

@SuppressWarnings("unused")
public class AgnesFuckingAround {
    public static void main(String... args) throws Exception {
        Logger logger = LoggerFactory.getLogger(AgnesFuckingAround.class);
        logger.info("Let's fuck around for a little while...");
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
//                "fuck_1";
//                "fuck_2";
                "fuck_3";

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

    private void fuck_2() {
        AgnesGirlRepository repo = getBackPlatform().getSpringctx().getBean(AgnesGirlRepository.class);

        {
            AgnesGirl girl = new AgnesGirl();
            girl.name = "Mandy the Mutant";
            {
                AgnesBoob boob = new AgnesBoob();
                boob.location = "middle";
                boob.girl = girl;
                girl.boobs.add(boob);
            }
            {
                AgnesBoob boob = new AgnesBoob();
                boob.location = "bottom";
                boob.girl = girl;
                girl.boobs.add(boob);
            }
            repo.save(girl);
            clog("\n============== After TX 1 ==============\n");
            dumpShit();
        }

        {
            getBackPlatform().tx(tx -> {
                AgnesGirl girl = repo.findOne(1L);
                girl.name = "Pizda";
                girl.boobs.remove(0);
                return null;
            });

            clog("\n============== After TX 2 ==============\n");
            dumpShit();
        }

    }

    private void fuck_3() {
        AgnesGirlRepository repo = getBackPlatform().getSpringctx().getBean(AgnesGirlRepository.class);

        {
            AgnesGirl girl = new AgnesGirl();
            girl.name = "Mandy the Mutant";
            {
                AgnesBoob boob = new AgnesBoob();
                boob.location = "middle";
                boob.girl = girl;
                girl.boobs.add(boob);
            }
            {
                AgnesBoob boob = new AgnesBoob();
                boob.location = "bottom";
                boob.girl = girl;
                girl.boobs.add(boob);
            }
            repo.save(girl);
            clog("\n============== After TX 1 ==============\n");
            dumpShit();
        }

        {
            new Object() {
                AgnesGirl girl = null;
                {
                    getBackPlatform().tx(tx -> {
                        girl = repo.findOne(1L);
                        girl.boobs.size();
                        girl.boobs.get(0).tits.size();
                        return null;
                    });

                    girl.name = "Pizda";
                    girl.boobs.remove(0);
                    {
                        AgnesBoob boob = new AgnesBoob();
                        boob.location = "rear";
                        boob.girl = girl;
                        girl.boobs.add(boob);
                    }
                    {
                        AgnesTit tit = new AgnesTit();
                        tit.description = "A crazy tit";
                        tit.boob = girl.boobs.get(0);
                        girl.boobs.get(0).tits.add(tit);
                    }
                    getBackPlatform().tx(tx -> {
                        girl = repo.save(girl);
                        return null;
                    });

                    clog("\n============== After TX 2 ==============\n");
                    dumpShit();

                    girl.boobs.remove(1);
                    getBackPlatform().tx(tx -> {
                        girl = repo.save(girl);
                        return null;
                    });

                    clog("\n============== After TX 3 ==============\n");
                    dumpShit();
                }
            };
        }

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









// EntityManager em = DBPile.INSTANCE.getJpaContext().getEntityManagerByManagedType(AgnesGirl.class);


