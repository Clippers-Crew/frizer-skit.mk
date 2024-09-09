package mk.frizer.unit;

import mk.frizer.domain.ImageEntity;
import mk.frizer.domain.Salon;
import mk.frizer.repository.ImageRepository;
import mk.frizer.repository.SalonRepository;
import mk.frizer.service.impl.ImageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private SalonRepository salonRepository;

    @InjectMocks
    private ImageServiceImpl imageService;

    @Test
    void testSaveImage() throws IOException {
        Long salonId = 1L;
        byte[] imageBytes = new byte[]{1, 2, 3};
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(imageBytes);
        Salon salon = new Salon();
        salon.setId(salonId);
        salon.setImages(new ArrayList<>());
        when(salonRepository.findById(salonId)).thenReturn(Optional.of(salon));
        when(salonRepository.save(any(Salon.class))).thenReturn(salon);
        ImageEntity savedImage = new ImageEntity(imageBytes, salonId);
        savedImage.setId(1L);
        when(imageRepository.save(any(ImageEntity.class))).thenReturn(savedImage);

        Optional<Salon> result = imageService.saveImage(salonId, file);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getImages().size());
        assertEquals(savedImage.getId(), result.get().getImages().get(0));
        verify(imageRepository, times(1)).save(any(ImageEntity.class));
        verify(salonRepository, times(1)).save(salon);
    }

    @Test
    void testSaveImage_SalonNotFound() throws IOException {
        Long salonId = 1L;
        MultipartFile file = mock(MultipartFile.class);
        when(salonRepository.findById(salonId)).thenReturn(Optional.empty());

        Optional<Salon> result = imageService.saveImage(salonId, file);

        assertFalse(result.isPresent());
    }

    @Test
    void testSaveBackgroundImage() throws IOException {
        Long salonId = 1L;
        byte[] imageBytes = new byte[]{1, 2, 3};
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(imageBytes);
        Salon salon = new Salon();
        salon.setId(salonId);
        salon.setBackgroundImage(2L);
        when(salonRepository.findById(salonId)).thenReturn(Optional.of(salon));
        when(salonRepository.save(any(Salon.class))).thenReturn(salon);

        ImageEntity newImage = new ImageEntity(imageBytes, salonId, true);
        newImage.setId(1L);
        when(imageRepository.save(any(ImageEntity.class))).thenReturn(newImage);
        doNothing().when(imageRepository).deleteById(anyLong());

        Optional<Salon> result = imageService.saveBackgroundImage(salonId, file);

        assertTrue(result.isPresent());
        assertEquals(newImage.getId(), result.get().getBackgroundImage());
        verify(imageRepository, times(1)).save(any(ImageEntity.class));
        verify(imageRepository, times(1)).deleteById(2L);
        verify(salonRepository, times(1)).save(salon);
    }

    @Test
    void testSaveBackgroundImage_SalonNotFound() throws IOException {
        Long salonId = 1L;
        MultipartFile file = mock(MultipartFile.class);
        when(salonRepository.findById(salonId)).thenReturn(Optional.empty());

        Optional<Salon> result = imageService.saveBackgroundImage(salonId, file);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetImage() {
        Long salonId = 1L;
        Long imageId = 2L;
        byte[] imageBytes = new byte[]{1, 2, 3};
        Salon salon = new Salon();
        salon.setId(salonId);
        salon.setBackgroundImage(imageId);
        when(salonRepository.findById(salonId)).thenReturn(Optional.of(salon));
        ImageEntity imageEntity = new ImageEntity(imageBytes, salonId);
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(imageEntity));

        byte[] result = imageService.getImage(salonId, imageId);

        assertArrayEquals(imageBytes, result);
    }

    @Test
    void testGetImage_ImageNotFound() {
        Long salonId = 1L;
        Long imageId = 2L;
        Salon salon = new Salon();
        salon.setId(salonId);
        salon.setBackgroundImage(imageId);
        when(salonRepository.findById(salonId)).thenReturn(Optional.of(salon));
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        byte[] result = imageService.getImage(salonId, imageId);

        assertNull(result);
    }
}
