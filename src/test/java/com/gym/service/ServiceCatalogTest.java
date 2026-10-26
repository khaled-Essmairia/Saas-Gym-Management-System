package com.gym.service;

import com.gym.entity.Rate;
import com.gym.repository.MemberRepository;
import com.gym.repository.RateRepository;
import com.gym.web.form.ServiceForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceCatalogTest {

    private RateRepository rates;
    private MemberRepository members;
    private ServiceCatalog catalog;
    private Rate fitness;
    private Rate sauna;

    @BeforeEach
    void setUp() {
        rates = mock(RateRepository.class);
        members = mock(MemberRepository.class);
        fitness = rate(1, "Fitness", "55");
        sauna = rate(2, "Sauna", "35");
        when(rates.findAll()).thenReturn(List.of(sauna, fitness));
        when(rates.findById(1)).thenReturn(Optional.of(fitness));
        when(rates.findById(2)).thenReturn(Optional.of(sauna));
        when(rates.findFirstByNameIgnoreCase("Fitness")).thenReturn(Optional.of(fitness));
        when(rates.findFirstByNameIgnoreCase("Sauna")).thenReturn(Optional.of(sauna));
        when(rates.findFirstByNameIgnoreCase("Yoga")).thenReturn(Optional.empty());
        when(members.countByServicesIgnoreCase("Fitness")).thenReturn(4L);
        catalog = new ServiceCatalog(rates, members);
    }

    private static Rate rate(int id, String name, String charge) {
        Rate r = new Rate();
        r.setId(id);
        r.setName(name);
        r.setCharge(charge);
        return r;
    }

    private static ServiceForm form(String name, Integer charge) {
        ServiceForm f = new ServiceForm();
        f.setName(name);
        f.setCharge(charge);
        return f;
    }

    @Test
    void listIsSortedAndCountsMembers() {
        List<ServiceCatalog.Row> rows = catalog.list();
        assertEquals("Fitness", rows.get(0).getName());
        assertEquals(4, rows.get(0).getMembers());
        assertEquals(55, rows.get(0).getCharge());
        assertEquals(0, rows.get(1).getMembers());
    }

    @Test
    void createsANewServiceAndRefusesADuplicateName() {
        assertNull(catalog.create(form("  Yoga ", 30)));
        verify(rates).save(any(Rate.class));

        when(rates.findFirstByNameIgnoreCase("fitness")).thenReturn(Optional.of(fitness));
        assertNotNull(catalog.create(form("fitness", 10)));
    }

    @Test
    void renamingAServiceRenamesItForItsMembers() {
        assertNull(catalog.update(1, form("Gym", 60)));

        verify(members).renameService("Fitness", "Gym");
        assertEquals("Gym", fitness.getName());
        assertEquals("60", fitness.getCharge());
    }

    @Test
    void changingOnlyThePriceLeavesMembersAlone() {
        assertNull(catalog.update(1, form("Fitness", 70)));

        verify(members, never()).renameService(any(), any());
        assertEquals(70, fitness.getChargeValue());
    }

    @Test
    void cannotTakeTheNameOfAnotherService() {
        assertNotNull(catalog.update(1, form("Sauna", 55)));
        verify(rates, never()).save(any(Rate.class));
    }

    @Test
    void aServiceWithMembersCannotBeDeleted() {
        assertNotNull(catalog.delete(1));
        verify(rates, never()).delete(any(Rate.class));
    }

    @Test
    void anUnusedServiceCanBeDeleted() {
        assertNull(catalog.delete(2));
        verify(rates).delete(sauna);
    }

    @Test
    void unknownServiceIsNotFound() {
        assertThrows(NotFoundException.class, () -> catalog.get(99));
    }
}
