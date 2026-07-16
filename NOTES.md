- linha 1 é a declaração de pacote (package): Ela diz a qual "pacote" essa classe pertence. Pacote em Java é basicamente uma pasta/namespace que organiza e agrupa suas classes.

- Um @ é uma annotation: um "adesivo" de metadado que você cola em cima da classe, atributo ou método. Ela não executa nada sozinha — outras ferramentas (JPA, Hibernate, Lombok, Spring) leem esses adesivos e geram comportamento a partir deles.

- Exception (Checked Exception) Se um método pode lançar uma Exception, o Java obriga você a tratar ou declarar. utilizando try/catch ou declarando na função chamadora do método.

- RuntimeException (Unchecked Exception) São exceções que o compilador não exige tratamento. Você pode ignorar — e se acontecer, o programa quebra em runtime.
