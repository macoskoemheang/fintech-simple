package com.example.m3showcase.feature.booking.di

import com.example.m3showcase.core.common.DateProvider
import com.example.m3showcase.core.di.AppScope
import com.example.m3showcase.feature.booking.data.BookingRepositoryImpl
import com.example.m3showcase.feature.booking.data.mcp.AssetReader
import com.example.m3showcase.feature.booking.data.mcp.BookingMcpServer
import com.example.m3showcase.feature.booking.data.mcp.InProcessMcpTransport
import com.example.m3showcase.feature.booking.data.mcp.McpClient
import com.example.m3showcase.feature.booking.data.mcp.McpConfig
import com.example.m3showcase.feature.booking.data.mcp.McpTransport
import com.example.m3showcase.feature.booking.domain.repository.BookingRepository
import com.example.m3showcase.feature.booking.domain.usecase.CancelBookingUseCase
import com.example.m3showcase.feature.booking.domain.usecase.CheckAvailabilityUseCase
import com.example.m3showcase.feature.booking.domain.usecase.ClearMcpTrafficUseCase
import com.example.m3showcase.feature.booking.domain.usecase.ConnectBookingServerUseCase
import com.example.m3showcase.feature.booking.domain.usecase.CreateBookingUseCase
import com.example.m3showcase.feature.booking.domain.usecase.GetBookingUseCase
import com.example.m3showcase.feature.booking.domain.usecase.GetBookingsUseCase
import com.example.m3showcase.feature.booking.domain.usecase.GetListingUseCase
import com.example.m3showcase.feature.booking.domain.usecase.ObserveBookingsVersionUseCase
import com.example.m3showcase.feature.booking.domain.usecase.ObserveMcpConnectionUseCase
import com.example.m3showcase.feature.booking.domain.usecase.ObserveMcpTrafficUseCase
import com.example.m3showcase.feature.booking.domain.usecase.RunMcpToolUseCase
import com.example.m3showcase.feature.booking.domain.usecase.SearchListingsUseCase
import com.example.m3showcase.feature.booking.presentation.detail.BookingDetailViewModel
import com.example.m3showcase.feature.booking.presentation.hub.BookingHubViewModel
import com.example.m3showcase.feature.booking.presentation.inspector.McpInspectorViewModel
import com.example.m3showcase.feature.booking.presentation.listing.ListingDetailViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val bookingModule = module {
    single { AssetReader { path -> androidContext().assets.open(path).bufferedReader().use { it.readText() } } }

    // MCP: server (defined by assets/mcp/booking-server.json) <- transport <- client.
    single {
        val assets = get<AssetReader>()
        val dates = get<DateProvider>()
        BookingMcpServer(
            // Read lazily, on the first request, which runs on the IO dispatcher.
            loadDefinition = { assets.read(McpConfig.parse(assets.read(McpConfig.Path)).server("booking").definition) },
            today = { dates.today() },
        )
    }
    single<McpTransport> { InProcessMcpTransport(server = get(), network = get()) }
    single { McpClient(transport = get()) }
    single<BookingRepository> { BookingRepositoryImpl(client = get(), assets = get(), dispatchers = get(), appScope = get(AppScope)) }

    factoryOf(::ObserveMcpConnectionUseCase)
    factoryOf(::ConnectBookingServerUseCase)
    factoryOf(::SearchListingsUseCase)
    factoryOf(::GetListingUseCase)
    factoryOf(::CheckAvailabilityUseCase)
    factoryOf(::CreateBookingUseCase)
    factoryOf(::GetBookingsUseCase)
    factoryOf(::ObserveBookingsVersionUseCase)
    factoryOf(::GetBookingUseCase)
    factoryOf(::CancelBookingUseCase)
    factoryOf(::ObserveMcpTrafficUseCase)
    factoryOf(::RunMcpToolUseCase)
    factoryOf(::ClearMcpTrafficUseCase)

    viewModelOf(::BookingHubViewModel)
    viewModelOf(::McpInspectorViewModel)
    viewModel { params ->
        ListingDetailViewModel(
            listingId = params.get(),
            getListing = get(),
            checkAvailability = get(),
            createBooking = get(),
            observeSession = get(),
            dateProvider = get(),
        )
    }
    viewModel { params ->
        BookingDetailViewModel(
            bookingId = params[0],
            isNew = params[1],
            getBooking = get(),
            cancelBooking = get(),
            dateProvider = get(),
        )
    }
}
