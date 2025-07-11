package com.example.SonicCanopy.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Entity
@Table(name = "club_roles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String roleName;

    @ManyToMany(mappedBy = "roles")
    private Set<ClubMember> clubMembers;

    private String roleDescription;
}
