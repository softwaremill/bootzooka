import { ProfileDetails } from './components/ProfileDetails';
import { PasswordDetails } from './components/PasswordDetails';

export const Profile: React.FC = () => (
  <div className="w-full h-full grid grid-rows-2 grid-cols-2 gap-4 p-4 lg:p-0">
    <ProfileDetails />
    <PasswordDetails />
  </div>
);
