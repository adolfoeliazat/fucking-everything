package agnes;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "agnes_boobs")
public class AgnesBoob {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(columnDefinition = "text")
    public String location;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinColumn(name = "girl_id")
    public AgnesGirl girl;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "boob", cascade = {CascadeType.ALL}, orphanRemoval = true)
    public List<AgnesTit> tits = new ArrayList<>();
}

