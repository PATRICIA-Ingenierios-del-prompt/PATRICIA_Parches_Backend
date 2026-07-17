package ingprompt.patricia.parches.application.service;

import ingprompt.patricia.parches.application.port.out.ParcheRepositoryOutPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParcheAdminServiceTest {

    @Mock private ParcheRepositoryOutPort parcheRepository;
    @InjectMocks private ParcheAdminService parcheAdminService;

    @Test
    void countParches_returnsRepositoryCount() {
        when(parcheRepository.count()).thenReturn(123L);

        long result = parcheAdminService.countParches();

        assertThat(result).isEqualTo(123L);
    }
}
