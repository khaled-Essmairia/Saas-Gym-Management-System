package com.gym.service;

import com.gym.entity.Attendance;
import com.gym.entity.Member;
import com.gym.repository.AttendanceRepository;
import com.gym.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/** Check in / check out (actions/check-attendance.php, actions/delete-attendance.php). */
@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendance;
    private final MemberRepository members;
    private final GymClock clock;

    public AttendanceService(AttendanceRepository attendance, MemberRepository members, GymClock clock) {
        this.attendance = attendance;
        this.members = members;
        this.clock = clock;
    }

    /** Today's check-ins, by member id. */
    @Transactional(readOnly = true)
    public Map<Integer, Attendance> todayByMember() {
        Map<Integer, Attendance> map = new HashMap<>();
        for (Attendance a : attendance.findByCurrDate(clock.todayText())) {
            try {
                map.putIfAbsent(Integer.valueOf(a.getUserId().trim()), a);
            } catch (NumberFormatException ignored) {
                // bad legacy row
            }
        }
        return map;
    }

    /** Records today's check-in and adds one day to the member's attendance count. */
    public boolean checkIn(Integer memberId) {
        Member m = members.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found: " + memberId));
        String today = clock.todayText();
        if (attendance.existsByUserIdAndCurrDate(String.valueOf(memberId), today)) {
            return false; // already checked in today
        }
        var now = clock.now();
        Attendance a = new Attendance();
        a.setUserId(String.valueOf(memberId));
        a.setCurrDate(today);
        a.setCurrTime(now.format(GymClock.ATTENDANCE_TIME));
        a.setPresent(true);
        attendance.save(a);

        m.setAttendanceCount(m.getAttendanceCount() + 1);
        members.save(m);
        return true;
    }

    /** Removes today's check-in. The attendance count is kept, as in the PHP version. */
    public void checkOut(Integer memberId) {
        attendance.deleteByUserIdAndCurrDate(String.valueOf(memberId), clock.todayText());
    }
}
