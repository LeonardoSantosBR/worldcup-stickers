- linha 1 é a declaração de pacote (package): Ela diz a qual "pacote" essa classe pertence. Pacote em Java é basicamente uma pasta/namespace que organiza e agrupa suas classes.

- Um @ é uma annotation: um "adesivo" de metadado que você cola em cima da classe, atributo ou método. Ela não executa nada sozinha — outras ferramentas (JPA, Hibernate, Lombok, Spring) leem esses adesivos e geram comportamento a partir deles.

- Exception (Checked Exception) Se um método pode lançar uma Exception, o Java obriga você a tratar ou declarar. utilizando try/catch ou declarando na função chamadora do método.

- RuntimeException (Unchecked Exception) São exceções que o compilador não exige tratamento. Você pode ignorar — e se acontecer, o programa quebra em runtime.

- Inversão de Controle (IoC) normalmente tu controlarias a criação dos objetos com new. Com IoC, tu entrega esse controle ao Spring — ele cria os objetos e te entrega prontos. O controle "inverte": sai das tuas mãos e vai pro container do Spring.

no UsersService.java:14-17:
private final UsersRepository usersRepository;
private final HashService hashService;

public UsersService(UsersRepository usersRepository, HashService hashService) {
    this.usersRepository = usersRepository;
    this.hashService = hashService;
}

o UsersService precisa de um UsersRepository e de um HashService, mas nunca faz new UsersRepository(). Ele só declara no construtor "eu preciso disso". O Spring vê o construtor, encontra esses objetos no container, e os injeta automaticamente. Isso é Injeção de Dependência — que é a forma mais comum de aplicar IoC.

- Bean um objeto que o Spring cria, gerencia e guarda no container pra injetar quando alguém precisar.