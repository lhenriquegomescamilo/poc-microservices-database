export const DATABASE_MICROSERVICE: IConfigMicroservice = {
    dev: {host: 'localhost', port: 3001},
};

export interface IConfigMicroservice {
    test?: { host: string, port: number };
    dev?: { host: string, port: number };
    hom?: { host: string, port: number };
    prehom?: { host: string, port: number };
    prod?: { host: string, port: number };
}
