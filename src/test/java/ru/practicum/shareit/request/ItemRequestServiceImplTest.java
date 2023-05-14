package ru.practicum.shareit.request;


import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.markers.Constants;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapperImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    private final int from = Integer.parseInt(Constants.PAGE_DEFAULT_FROM);
    private final int size = Integer.parseInt(Constants.PAGE_DEFAULT_SIZE);
    private final Pageable pageable = PageRequest.of(from / size, size);
    private final LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
    private final User user1 = User.builder()
            .id(1L)
            .name("Test user 1")
            .email("tester1@ya.ru")
            .build();
    private final User user2 = User.builder()
            .id(2L)
            .name("Test user 2")
            .email("tester2@ya.ru")
            .build();
    private final Item item1 = Item.builder()
            .id(1L)
            .name("item name")
            .description("item description")
            .available(true)
            .owner(user1)
            .requestId(1L)
            .build();
    private final ItemDto itemDto1 = ItemDto.builder()
            .id(item1.getId())
            .name(item1.getName())
            .description(item1.getDescription())
            .available(item1.getAvailable())
            .ownerId(item1.getOwner().getId())
            .requestId(item1.getRequestId())
            .build();
    private final ItemRequestAddDto item1RequestCreateDto = ItemRequestAddDto.builder()
            .description("item description")
            .build();
    private final ItemRequest itemRequest1 = ItemRequest.builder()
            .id(1L)
            .description("itemRequest1 description")
            .requestorId(user2)
            .created(dateTime)
            .build();
    private final ItemRequestExtendedDto itemRequestExtendedDto1 = ItemRequestExtendedDto.builder()
            .id(itemRequest1.getId())
            .description(itemRequest1.getDescription())
            .created(itemRequest1.getCreated())
            .items(List.of(itemDto1))
            .build();
    @Mock
    private UserService userService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRequestMapperImpl itemRequestMapper;
    @Mock
    private ItemMapperImpl itemMapper;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    @Captor
    private ArgumentCaptor<ItemRequest> itemRequestArgumentCaptor;

    private void checkItemRequestExtendedDto(ItemRequest itemRequest1, ItemRequestExtendedDto itemRequestExtendedDto) {
        assertEquals(itemRequest1.getId(), itemRequestExtendedDto.getId());
        assertEquals(itemRequest1.getDescription(), itemRequestExtendedDto.getDescription());
        assertEquals(itemRequest1.getCreated(), itemRequestExtendedDto.getCreated());

        Item item = item1;
        ItemDto resultItemDto = itemRequestExtendedDto.getItems().get(0);

        assertEquals(item.getId(), resultItemDto.getId());
        assertEquals(item.getName(), resultItemDto.getName());
        assertEquals(item.getAvailable(), resultItemDto.getAvailable());
        assertEquals(item.getDescription(), resultItemDto.getDescription());
        assertEquals(item.getOwner().getId(), resultItemDto.getOwnerId());
    }

    @Nested
    class Create {
        @Test
        public void shouldCreate() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(itemRequestMapper.toItemRequest(any(), any(), any())).thenCallRealMethod();
            when(itemRequestRepository.save(any())).thenReturn(itemRequest1);
            when(itemRequestMapper.toItemRequestDto(any())).thenCallRealMethod();

            ItemRequestDto result = itemRequestService.add(user2.getId(), item1RequestCreateDto);

            verify(itemRequestRepository, times(1)).save(itemRequestArgumentCaptor.capture());
            verify(itemRequestMapper, times(1)).toItemRequest(any(), any(), any());

            ItemRequest savedItemRequest = itemRequestArgumentCaptor.getValue();
            savedItemRequest.setId(result.getId());

            assertEquals(itemRequest1, savedItemRequest);
            assertEquals(item1RequestCreateDto.getDescription(), savedItemRequest.getDescription());
            assertEquals(user2.getId(), savedItemRequest.getRequestorId().getId());
            assertEquals(user2.getName(), savedItemRequest.getRequestorId().getName());
            assertEquals(user2.getEmail(), savedItemRequest.getRequestorId().getEmail());
            assertNotNull(savedItemRequest.getCreated());
        }
    }

    @Nested
    class GetById {
        @Test
        public void shouldGet() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest1));
            when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item1));
            when(itemMapper.toItemDto(any())).thenCallRealMethod();
            when(itemRequestMapper.toItemRequestExtendedDto(any(), any())).thenCallRealMethod();

            ItemRequestExtendedDto result = itemRequestService.getById(user2.getId(), 1L);

            checkItemRequestExtendedDto(itemRequest1, result);
            verify(userService, times(1)).getUserById(user2.getId());
            verify(itemRequestRepository, times(1)).findById(1L);
            verify(itemRepository, times(1)).findByRequestId(1L);
            verify(itemMapper, times(1)).toItemDto(any());
            verify(itemRequestMapper, times(1)).toItemRequestExtendedDto(any(), any());
        }

        @Test
        public void shouldThrowExceptionIfItemRequestIdNotFound() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(itemRequestRepository.findById(1L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> itemRequestService.getById(user2.getId(), 1L));
            assertEquals("Запроса вещи с таким id не существует.", exception.getMessage());
            verify(userService, times(1)).getUserById(user2.getId());
            verify(itemRequestRepository, times(1)).findById(1L);
        }
    }

    @Nested
    class GetByRequestorId {
        @Test
        public void shouldGet() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(itemRequestRepository.findByRequestorId_IdOrderByCreatedAsc(user2.getId()))
                    .thenReturn(List.of(itemRequest1));
            when(itemRepository.findByRequestIdIn(List.of(1L))).thenReturn(List.of(item1));
            when(itemMapper.toItemDto(any())).thenCallRealMethod();
            when(itemRequestMapper.toItemRequestExtendedDto(any(), any())).thenCallRealMethod();

            List<ItemRequestExtendedDto> results = itemRequestService.getByRequestorId(user2.getId());

            assertEquals(1, results.size());

            ItemRequestExtendedDto result = results.get(0);

            checkItemRequestExtendedDto(itemRequest1, result);
            verify(userService, times(1)).getUserById(user2.getId());
            verify(itemRequestRepository, times(1))
                    .findByRequestorId_IdOrderByCreatedAsc(user2.getId());
            verify(itemRepository, times(1)).findByRequestIdIn(List.of(1L));
            verify(itemMapper, times(1)).toItemDto(any());
            verify(itemRequestMapper, times(1)).toItemRequestExtendedDto(any(), any());
        }

        @Test
        public void shouldGetEmptyIfNotItemRequests() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(itemRequestRepository.findByRequestorId_IdOrderByCreatedAsc(user1.getId()))
                    .thenReturn(List.of());

            List<ItemRequestExtendedDto> results = itemRequestService.getByRequestorId(user1.getId());

            assertTrue(results.isEmpty());
            verify(userService, times(1)).getUserById(user1.getId());
            verify(itemRequestRepository, times(1))
                    .findByRequestorId_IdOrderByCreatedAsc(user1.getId());
        }
    }

    @Nested
    class GetAll {
        @Test
        public void shouldGetNotSelfRequests() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(itemRequestRepository.findByRequestorId_IdNot(user1.getId(), pageable))
                    .thenReturn(new PageImpl<>(List.of(itemRequest1)));
            when(itemRequestMapper.toItemRequestExtendedDto(any(), any())).thenCallRealMethod();

            List<ItemRequestExtendedDto> results = itemRequestService.getAll(user1.getId(), pageable);


            assertEquals(1, results.size());

            ItemRequestExtendedDto result = results.get(0);
            result.setItems(itemRequestExtendedDto1.getItems());

            checkItemRequestExtendedDto(itemRequest1, result);
            verify(userService, times(1)).getUserById(user1.getId());
            verify(itemRequestRepository, times(1))
                    .findByRequestorId_IdNot(user1.getId(), pageable);
            verify(itemRequestMapper, times(1)).toItemRequestExtendedDto(any(), any());
        }

        @Test
        public void shouldGetEmptyIfNotRequests() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(itemRequestRepository.findByRequestorId_IdNot(user1.getId(), pageable))
                    .thenReturn(new PageImpl<>(List.of()));

            List<ItemRequestExtendedDto> results = itemRequestService.getAll(user1.getId(), pageable);

            assertTrue(results.isEmpty());
            verify(userService, times(1)).getUserById(user1.getId());
            verify(itemRequestRepository, times(1))
                    .findByRequestorId_IdNot(user1.getId(), pageable);
        }
    }
}