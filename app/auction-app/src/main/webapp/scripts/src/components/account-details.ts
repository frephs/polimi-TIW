import { User } from '../prototypes';

export function updateUserDetails(user: User) {
    document.querySelector('#logout h1 span')!.textContent = user.name;
    document.querySelector('#account-details p span')!.textContent = user.username;

    (document.querySelector('#username') as HTMLInputElement).value = user.username;
    (document.querySelector('#name') as HTMLInputElement).value = user.name;
    (document.querySelector('#surname') as HTMLInputElement).value = user.surname;

    (document.querySelector('#country') as HTMLInputElement).value = user.address.country;
    (document.querySelector('#zip-code') as HTMLInputElement).value =
        user.address.zipCode.toString();
    (document.querySelector('#city') as HTMLInputElement).value = user.address.city;
    (document.querySelector('#street') as HTMLInputElement).value = user.address.street;
    (document.querySelector('#street-number') as HTMLInputElement).value =
        user.address.streetNumber.toString();
}
