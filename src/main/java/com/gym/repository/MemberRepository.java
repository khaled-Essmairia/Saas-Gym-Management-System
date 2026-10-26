package com.gym.repository;

import com.gym.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    List<Member> findByStatusOrderByUserId(String status);

    List<Member> findByUsernameOrderByUserId(String username);

    long countByStatus(String status);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndUserIdNot(String username, Integer userId);

    /** Number of members who use a service (members keep the service by its name). */
    long countByServicesIgnoreCase(String services);

    /** When a service is renamed, its members follow. */
    @Modifying
    @Query("update Member m set m.services = :newName where lower(m.services) = lower(:oldName)")
    int renameService(@Param("oldName") String oldName, @Param("newName") String newName);

    @Query("select m from Member m where lower(m.fullname) like lower(concat('%', :q, '%')) "
            + "or lower(m.username) like lower(concat('%', :q, '%')) order by m.userId")
    List<Member> search(String q);

    @Query("select coalesce(sum(m.amount), 0) from Member m")
    long totalEarnings();

    @Query("select m.services, count(m) from Member m group by m.services order by m.services")
    List<Object[]> countByServices();

    @Query("select m.gender, count(m) from Member m group by m.gender order by m.gender")
    List<Object[]> countByGender();
}
