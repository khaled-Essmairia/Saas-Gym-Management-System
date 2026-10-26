package com.gym.service;

import com.gym.entity.Rate;
import com.gym.web.form.EquipmentForm;
import com.gym.web.form.MemberForm;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImportServiceTest {

    private static final String MEMBER_HEADER = "fullname,username,password,gender,dor,services,plan,amount,address,contact\r\n";
    private static final String EQUIPMENT_HEADER = "name,description,date,quantity,vendor,address,contact,amount\r\n";

    private MemberService members;
    private EquipmentService equipment;
    private ImportService service;

    @BeforeEach
    void setUp() {
        members = mock(MemberService.class);
        equipment = mock(EquipmentService.class);
        when(members.rates()).thenReturn(List.of(rate("Fitness", "55"), rate("Sauna", "35")));
        when(members.usernameTaken("existing", null)).thenReturn(true);

        GymClock clock = mock(GymClock.class);
        when(clock.today()).thenReturn(LocalDate.of(2026, 10, 26));

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        service = new ImportService(members, equipment, validator, clock);
    }

    private static Rate rate(String name, String charge) {
        Rate r = new Rate();
        r.setName(name);
        r.setCharge(charge);
        return r;
    }

    private static InputStream csv(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void importsMembersFromAnExcelFile() throws Exception {
        String file = "\uFEFFsep=;\r\n" + MEMBER_HEADER.replace(',', ';')
                + "Sara Ben Ali;sara.b;Pass123;female;26/10/2026;fitness;3;;Tunis;21612345678\r\n"
                + "Ali Trabelsi;ali.t;Pass456;Male;;Sauna;1;40.0;Ariana;21698765432\r\n";

        ImportService.Result result = service.importMembers(csv(file));

        assertTrue(result.isOk(), () -> result.getErrors().toString());
        ArgumentCaptor<MemberForm> saved = ArgumentCaptor.forClass(MemberForm.class);
        verify(members, times(2)).create(saved.capture());
        MemberForm first = saved.getAllValues().get(0);
        assertEquals("Female", first.getGender());
        assertEquals("Fitness", first.getServices());
        assertEquals(55, first.getAmount());           // monthly rate of the service
        assertEquals(LocalDate.of(2026, 10, 26), first.getDor());
        assertEquals(40, saved.getAllValues().get(1).getAmount());
    }

    @Test
    void oneBadLineMeansNothingIsSavedAndEveryProblemIsListed() throws Exception {
        String file = MEMBER_HEADER
                + "Sara Ben Ali,sara.b,Pass123,Female,2026-10-26,Fitness,3,,Tunis,21612345678\r\n"
                + "Bob,existing,,Robot,2026-13-40,Gym,5,abc,Tunis,12ab\r\n"
                + "Sara Again,SARA.B,x,Male,,Sauna,1,,Tunis,2161234567\r\n";

        ImportService.Result result = service.importMembers(csv(file));

        assertFalse(result.isOk());
        assertEquals(0, result.getImported());
        verify(members, never()).create(any(MemberForm.class));
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrors().get(0).startsWith("Line 3:"));
        assertTrue(result.getErrors().get(0).contains("already taken"));
        assertTrue(result.getErrors().get(0).contains("Unknown service 'Gym'"));
        assertTrue(result.getErrors().get(1).startsWith("Line 4:"));
        assertTrue(result.getErrors().get(1).contains("also used on line 2"));
    }

    @Test
    void reportsMissingColumnsAndEmptyFiles() throws Exception {
        assertTrue(service.importMembers(csv("fullname,username\r\nA,b\r\n")).getErrors().get(0).startsWith("Missing column(s):"));
        assertEquals("The file is empty.", service.importMembers(csv("")).getErrors().get(0));
        assertTrue(service.importMembers(csv(MEMBER_HEADER)).getErrors().get(0).startsWith("The file has no data"));
    }

    @Test
    void importsEquipmentAndRejectsBadDates() throws Exception {
        ImportService.Result ok = service.importEquipment(csv(EQUIPMENT_HEADER
                + "Treadmill,\"Running machine, 20 km/h\",2026-10-26,2,Sport Pro,Tunis,21671234567,1200\r\n"
                + "Bench,Flat bench,,1,Fit Shop,Sfax,21674123456,300\r\n"));
        assertTrue(ok.isOk(), () -> ok.getErrors().toString());
        ArgumentCaptor<EquipmentForm> saved = ArgumentCaptor.forClass(EquipmentForm.class);
        verify(equipment, times(2)).create(saved.capture());
        assertEquals("Running machine, 20 km/h", saved.getAllValues().get(0).getDescription());
        assertEquals(LocalDate.of(2026, 10, 26), saved.getAllValues().get(1).getDate());

        ImportService.Result bad = service.importEquipment(csv(EQUIPMENT_HEADER + "Bike,Spin,2026-02-30,0,V,A,12,x\r\n"));
        assertFalse(bad.isOk());
        assertTrue(bad.getErrors().get(0).contains("Date '2026-02-30' is not valid"));
    }

    @Test
    void theDownloadableTemplatesAreValidFiles() throws Exception {
        assertTrue(service.importMembers(csv(service.memberTemplate())).isOk());
        assertTrue(service.importEquipment(csv(service.equipmentTemplate())).isOk());
    }
}
