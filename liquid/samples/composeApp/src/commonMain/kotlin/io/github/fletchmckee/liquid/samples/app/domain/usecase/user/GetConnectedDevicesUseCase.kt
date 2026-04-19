package io.github.fletchmckee.liquid.samples.app.domain.usecase.user

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Device
import io.github.fletchmckee.liquid.samples.app.domain.repository.UserRepository

/**
 * Use case for getting connected devices.
 */
class GetConnectedDevicesUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<List<Device>> {
        return userRepository.getConnectedDevices()
    }
}
