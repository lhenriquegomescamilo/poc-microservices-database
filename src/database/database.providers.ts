import {Sequelize} from 'sequelize-typescript';
import {User} from '../users/user.entity';

const port = (process.env.MYSQL_PORT || 3306) as number;
const host = (process.env.MYSQL_URL || 'localhost') as string;
export const DatabaseProviders = [
    {
        provide: 'SequelizeToken',
        useFactory: async () => {
            const sequelize = new Sequelize({
                operatorsAliases: false,
                dialect: 'mysql',
                host,
                port,
                username: 'root',
                password: 'ENGLAND_IS_MY_CITY',
                database: 'microservices',
            });
            sequelize.addModels([User]);
            await sequelize.sync();
            return sequelize;
        },
    },
];
