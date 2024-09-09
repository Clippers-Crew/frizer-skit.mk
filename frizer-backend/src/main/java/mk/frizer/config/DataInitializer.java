package mk.frizer.config;

import jakarta.annotation.PostConstruct;
import mk.frizer.domain.*;
import mk.frizer.domain.dto.*;
import mk.frizer.repository.CityRepository;
import mk.frizer.service.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer {
    private final BaseUserService baseUserService;
    private final BusinessOwnerService businessOwnerService;
    private final SalonService salonService;
    private final TreatmentService treatmentService;
    private final TagService tagService;
    private final EmployeeService employeeService;
    private final CustomerService customerService;
    private final ReviewService reviewService;
    private final CityRepository cityRepository;

    public DataInitializer(BaseUserService baseUserService, BusinessOwnerService businessOwnerService, SalonService salonService, TreatmentService treatmentService, TagService tagService, EmployeeService employeeService, CustomerService customerService, ReviewService reviewService, CityRepository cityRepository) {
        this.baseUserService = baseUserService;
        this.businessOwnerService = businessOwnerService;
        this.salonService = salonService;
        this.treatmentService = treatmentService;
        this.tagService = tagService;
        this.employeeService = employeeService;
        this.customerService = customerService;
        this.reviewService = reviewService;
        this.cityRepository = cityRepository;
    }

    @PostConstruct
    public void init() {
        boolean init = false;
        if (init) {

        List<String> all_cities = Arrays.asList(
                "Цела Македонија", "Берово", "Битола", "Богданци", "Валандово", "Велес", "Виница", "Гевгелија",
                "Гостивар", "Дебар", "Делчево", "Демир Капија", "Демир Хисар", "Кавадарци",
                "Кичево", "Кочани", "Кратово", "Крива Паланка", "Крушево", "Куманово",
                "Македонски Брод", "Македонска Каменица", "Неготино", "Охрид", "Пехчево",
                "Прилеп", "Пробиштип", "Радовиш", "Ресен", "Свети Николе", "Скопје",
                "Струга", "Струмица", "Тетово", "Штип");

        if (cityRepository.findAll().isEmpty()) {
            for (String city : all_cities) {
                cityRepository.save(new City(city));
            }
        }

            // Initialize the entities
            baseUserService.createBaseUser(new BaseUserAddDTO("user1@email.com", "password", "Aleksandar", "Jovanovski", "071234567"));
            baseUserService.createBaseUser(new BaseUserAddDTO("user2@email.com", "password", "Elena", "Petrova", "078765432"));
            baseUserService.createBaseUser(new BaseUserAddDTO("user3@email.com", "password", "Igor", "Nikoloski", "070111222"));
            baseUserService.createBaseUser(new BaseUserAddDTO("user4@email.com", "password", "Marija", "Georgieva", "072333444"));
            baseUserService.createBaseUser(new BaseUserAddDTO("user5@email.com", "password", "Viktor", "Stojanovski", "075555666"));

            // Get created users
            List<BaseUser> baseUsers = baseUserService.getBaseUsers();

            // Create Business Owners
            baseUsers.forEach(user -> businessOwnerService.createBusinessOwner(user.getId()));
            List<BusinessOwner> businessOwners = businessOwnerService.getBusinessOwners();

            // Create Tags
            tagService.createTag("Фризура");
            tagService.createTag("Шишање");
            tagService.createTag("Маникир");
            tagService.createTag("Педикир");
            tagService.createTag("Боење на коса");

            List<Tag> tags = tagService.getTags();

            // Create 20 Salons with 2-3 treatments each
            String[] salonNames = {
                    "Фризерски салон Стил", "Салон за убавина Александра", "Фризерски студио Елит", "Студио за убавина Моника",
                    "Фризерски салон Тина", "Салон за убавина Естетика", "Фризерски салон Коса", "Салон за убавина Гламур",
                    "Фризерски салон Бисера", "Студио за убавина Оаза", "Фризерски салон Мартина", "Салон за убавина Софи",
                    "Фризерски салон Елеганција", "Салон за убавина Мистик", "Фризерски студио Нина", "Салон за убавина Лукс",
                    "Фризерски салон Ивона", "Салон за убавина Златен пресек", "Фризерски салон Перфекција", "Салон за убавина Ванеса"
            };

            String[] addresses = {
                    "Улица 1", "Улица 2", "Улица 3", "Улица 4", "Улица 5", "Улица 6", "Улица 7", "Улица 8", "Улица 9", "Улица 10",
                    "Улица 11", "Улица 12", "Улица 13", "Улица 14", "Улица 15", "Улица 16", "Улица 17", "Улица 18", "Улица 19", "Улица 20"
            };

            String[] cities = {
                    "Скопје", "Прилеп", "Битола", "Охрид", "Велес", "Штип", "Тетово", "Куманово", "Струга", "Гостивар",
                    "Кавадарци", "Кочани", "Гевгелија", "Кичево", "Крива Паланка", "Ресен", "Радовиш", "Струмица", "Дебар", "Виница"
            };

            float[][] coordinates = {
                    {41.9981f, 21.4254f}, {41.3455f, 21.5550f}, {41.0328f, 21.3403f}, {41.1172f, 20.8016f},
                    {41.7272f, 21.7750f}, {41.7420f, 22.1990f}, {42.0097f, 20.9716f}, {42.1322f, 21.7141f},
                    {41.1783f, 20.6787f}, {41.8000f, 20.9062f}, {41.4326f, 21.9983f}, {41.9180f, 22.4189f},
                    {41.1393f, 22.5049f}, {41.5146f, 20.9574f}, {42.2019f, 22.3318f}, {41.0883f, 21.0122f},
                    {41.6383f, 22.4641f}, {41.4414f, 22.6420f}, {41.5245f, 20.5297f}, {41.8820f, 22.5075f}
            };

            for (int i = 0; i < salonNames.length; i++) {
                BusinessOwner owner = businessOwners.get(i % businessOwners.size());
                SalonAddDTO salonAddDTO = new SalonAddDTO(salonNames[i], "Салон за убавина", addresses[i], cities[i], "07000000" + i, owner.getId(), coordinates[i][0], coordinates[i][1]);
                salonService.createSalon(salonAddDTO);
            }

            // Create 2-3 Treatments for each Salon
            List<Salon> salons = salonService.getSalons();
            for (Salon salon : salons) {
                treatmentService.createTreatment(new TreatmentAddDTO("Шишање", salon.getId(), 300.0, 1));
                treatmentService.createTreatment(new TreatmentAddDTO("Миење коса", salon.getId(), 150.0, 1));
                if (salon.getId() % 2 == 0) {
                    treatmentService.createTreatment(new TreatmentAddDTO("Боење коса", salon.getId(), 600.0, 1));
                }
            }

            // Create Employees and assign them to Salons
            for (BaseUser user : baseUsers) {
                for (int i = 0; i < 4; i++) { // each user has 4 employees
                    Salon salon = salons.get((int) (i + user.getId()) % salons.size());
                    employeeService.createEmployee(new EmployeeAddDTO(user.getId(), salon.getId()));
                }
            }

            // Create Reviews
            for (int i = 0; i < 5; i++) {
                Salon salon = salons.get(i);
                Customer customer = customerService.getCustomerByBaseUserId(baseUsers.get(i).getId()).get();
                Employee employee = employeeService.getEmployeeByBaseUserId(customer.getId()).get();
                reviewService.createReviewForEmployee(new ReviewAddDTO(employee.getId(), customer.getId(), 5.0, "Одлична услуга!"));
            }
        }
    }
}

