package com.gym.service;

import com.gym.entity.Member;
import com.gym.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Counters and chart data. Replaces the small PHP include files
 * (dashboard-usercount.php, income-count.php, actions/count-*.php, ...).
 * Also used directly from templates, e.g. ${@statsService.memberCount()}.
 */
@Service
@Transactional(readOnly = true)
public class StatsService {

    private final MemberRepository members;
    private final StaffRepository staffs;
    private final EquipmentRepository equipment;
    private final AnnouncementRepository announcements;
    private final AttendanceRepository attendance;
    private final GymClock clock;

    public StatsService(MemberRepository members, StaffRepository staffs, EquipmentRepository equipment,
                        AnnouncementRepository announcements, AttendanceRepository attendance, GymClock clock) {
        this.members = members;
        this.staffs = staffs;
        this.equipment = equipment;
        this.announcements = announcements;
        this.attendance = attendance;
        this.clock = clock;
    }

    public long memberCount() { return members.count(); }

    public long activeMemberCount() { return members.countByStatus(Member.ACTIVE); }

    public long equipmentCount() { return equipment.count(); }

    public long staffCount() { return staffs.count(); }

    public long trainerCount() { return staffs.countByDesignation("Trainer"); }

    public long announcementCount() { return announcements.count(); }

    /** Members checked in today. */
    public long presentToday() { return attendance.countByCurrDate(clock.todayText()); }

    public long totalEarnings() { return members.totalEarnings(); }

    public long totalExpenses() { return equipment.totalExpenses(); }

    public Map<String, Long> membersByService() { return toMap(members.countByServices()); }

    public Map<String, Long> membersByGender() { return toMap(members.countByGender()); }

    public Map<String, Long> staffByDesignation() { return toMap(staffs.countByDesignationGroup()); }

    private static Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = row[0] == null || row[0].toString().isBlank() ? "Unknown" : row[0].toString();
            map.merge(key, ((Number) row[1]).longValue(), Long::sum);
        }
        return map;
    }
}
