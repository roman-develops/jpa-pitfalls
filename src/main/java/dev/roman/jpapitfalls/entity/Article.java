package dev.roman.jpapitfalls.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Table(name = "article")
@Entity
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;


    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY)
    private List<Comment> comments;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY)
    private List<Comment> commentsSubSelect;

}
