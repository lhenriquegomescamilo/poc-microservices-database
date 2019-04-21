import {NestMicroserviceOptions} from '@nestjs/common/interfaces/microservices/nest-microservice-options.interface';
import {MicroserviceOptions} from '@nestjs/common/interfaces/microservices/microservice-configuration.interface';
import {Transport} from '@nestjs/common/enums/transport.enum';

const port = (process.env.PORT_MICROSERVICE_DATABASE || 3001) as number;
export const OPTIONS: NestMicroserviceOptions & MicroserviceOptions = {
    transport: Transport.TCP,
    options: {port},
};
