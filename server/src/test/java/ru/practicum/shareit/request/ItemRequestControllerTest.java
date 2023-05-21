package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.markers.Constants;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.conroller.UserController;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRequestControllerTest {
    private final UserController userController;
    private final ItemController itemController;
    private final ItemRequestController itemRequestController;

    @Nested
    class Add {
        @Test
        public void shouldAdd() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@ya.ru")
                    .build();
            userController.add(userDto1);

            ItemRequestAddDto itemRequestCreateDto = ItemRequestAddDto.builder()
                    .description("description")
                    .build();

            ItemRequestDto itemRequestDto = itemRequestController.add(userDto1.getId(), itemRequestCreateDto);

            assertEquals(1L, itemRequestDto.getId());
            assertEquals(itemRequestCreateDto.getDescription(), itemRequestDto.getDescription());
            assertNotNull(itemRequestDto.getCreated());
        }
    }

    @Nested
    class GetById {
        @Test
        public void shouldGetWithItems() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@ya.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@ya.ru")
                    .build();
            userController.add(userDto2);

            ItemRequestAddDto itemRequestCreateDto = ItemRequestAddDto.builder()
                    .description("description")
                    .build();
            ItemRequestDto itemRequestDto = itemRequestController.add(userDto1.getId(), itemRequestCreateDto);

            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item")
                    .description("Test item description")
                    .available(true)
                    .ownerId(userDto2.getId())
                    .requestId(itemRequestDto.getId())
                    .build();
            itemController.add(itemDto.getOwnerId(), itemDto);

            ItemRequestExtendedDto itemRequestFromController = itemRequestController.getById(userDto1.getId(), itemRequestDto.getId());

            assertEquals(1L, itemRequestFromController.getId());
            assertEquals(itemRequestCreateDto.getDescription(), itemRequestFromController.getDescription());
            assertNotNull(itemRequestFromController.getCreated());

            assertNotNull(itemRequestFromController.getItems());
            assertEquals(1, itemRequestFromController.getItems().size());
            assertEquals(itemDto.getId(), itemRequestFromController.getItems().get(0).getId());
            assertEquals(itemDto.getDescription(), itemRequestFromController.getItems().get(0).getDescription());
            assertEquals(itemDto.getAvailable(), itemRequestFromController.getItems().get(0).getAvailable());
            assertEquals(itemDto.getOwnerId(), itemRequestFromController.getItems().get(0).getOwnerId());
            assertEquals(itemDto.getRequestId(), itemRequestFromController.getItems().get(0).getRequestId());
        }
    }

    @Nested
    class GetByRequestorId {
        @Test
        public void shouldGetWithItems() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@ya.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@ya.ru")
                    .build();
            userController.add(userDto2);

            ItemRequestAddDto itemRequestCreateDto = ItemRequestAddDto.builder()
                    .description("description")
                    .build();
            ItemRequestDto itemRequestDto = itemRequestController.add(userDto1.getId(), itemRequestCreateDto);

            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item")
                    .description("Test item description")
                    .available(true)
                    .ownerId(userDto2.getId())
                    .requestId(itemRequestDto.getId())
                    .build();
            itemController.add(itemDto.getOwnerId(), itemDto);

            List<ItemRequestExtendedDto> itemRequestsFromController = itemRequestController.getByRequestorId(userDto1.getId());

            assertEquals(1, itemRequestsFromController.size());

            ItemRequestExtendedDto itemRequestFromController = itemRequestsFromController.get(0);

            assertEquals(1L, itemRequestFromController.getId());
            assertEquals(itemRequestCreateDto.getDescription(), itemRequestFromController.getDescription());
            assertNotNull(itemRequestFromController.getCreated());

            assertNotNull(itemRequestFromController.getItems());
            assertEquals(1, itemRequestFromController.getItems().size());
            assertEquals(itemDto.getId(), itemRequestFromController.getItems().get(0).getId());
            assertEquals(itemDto.getDescription(), itemRequestFromController.getItems().get(0).getDescription());
            assertEquals(itemDto.getAvailable(), itemRequestFromController.getItems().get(0).getAvailable());
            assertEquals(itemDto.getOwnerId(), itemRequestFromController.getItems().get(0).getOwnerId());
            assertEquals(itemDto.getRequestId(), itemRequestFromController.getItems().get(0).getRequestId());
        }
    }

    @Nested
    class GetAll {
        @Test
        public void shouldGetAllWhereNotOwner() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@ya.ru")
                    .build();
            userController.add(userDto1);

            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@ya.ru")
                    .build();
            userController.add(userDto2);

            ItemRequestAddDto itemRequestCreateDto = ItemRequestAddDto.builder()
                    .description("description")
                    .build();
            ItemRequestDto itemRequestDto = itemRequestController.add(userDto1.getId(), itemRequestCreateDto);

            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item")
                    .description("Test item description")
                    .available(true)
                    .ownerId(userDto2.getId())
                    .requestId(itemRequestDto.getId())
                    .build();
            itemController.add(itemDto.getOwnerId(), itemDto);

            List<ItemRequestExtendedDto> itemRequestsFromController = itemRequestController.getAll(
                    userDto2.getId(),
                    Integer.parseInt(Constants.PAGE_DEFAULT_FROM),
                    Integer.parseInt(Constants.PAGE_DEFAULT_SIZE));

            assertEquals(1, itemRequestsFromController.size());

            ItemRequestExtendedDto itemRequestFromController = itemRequestsFromController.get(0);

            assertEquals(1L, itemRequestFromController.getId());
            assertEquals(itemRequestCreateDto.getDescription(), itemRequestFromController.getDescription());
            assertNotNull(itemRequestFromController.getCreated());

            assertNotNull(itemRequestFromController.getItems());
            assertEquals(1, itemRequestFromController.getItems().size());
            assertEquals(itemDto.getId(), itemRequestFromController.getItems().get(0).getId());
            assertEquals(itemDto.getDescription(), itemRequestFromController.getItems().get(0).getDescription());
            assertEquals(itemDto.getAvailable(), itemRequestFromController.getItems().get(0).getAvailable());
            assertEquals(itemDto.getOwnerId(), itemRequestFromController.getItems().get(0).getOwnerId());
            assertEquals(itemDto.getRequestId(), itemRequestFromController.getItems().get(0).getRequestId());
        }
    }
}