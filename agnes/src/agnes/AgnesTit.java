package agnes;

import javax.persistence.*;

@Entity @Table(name = "agnes_tits")
public class AgnesTit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(columnDefinition = "text")
    public String description;

    @ManyToOne(/*fetch = FetchType.EAGER,*/ /*cascade = {CascadeType.ALL}*/)
    @JoinColumn(name = "boob_id")
    public AgnesBoob boob;
}

