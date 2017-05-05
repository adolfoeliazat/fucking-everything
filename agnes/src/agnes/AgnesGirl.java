package agnes;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "agnes_girls")
public class AgnesGirl {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(columnDefinition = "text")
    public String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "girl", cascade = {CascadeType.ALL}, orphanRemoval = true)
    public List<AgnesBoob> boobs = new ArrayList<>();
}

