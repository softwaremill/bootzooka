import { useGetAdminVersion } from '@/api/apiComponents';
import type { FC } from 'react';
import { SiGithub } from '@icons-pack/react-simple-icons';
import { Tooltip, TooltipContent, TooltipTrigger } from '../ui/tooltip';
import { InfoIcon } from 'lucide-react';

export const AppFooter: FC = () => {
  const mutation = useGetAdminVersion({});

  return (
    <div className="w-full h-full flex flex-col justify-center lg:items-center lg:justify-between lg:flex-row text-center lg:text-left gap-4">
      <div>
        <div className="flex flex-col text-sm">
          <span className="text-xl text-blue-400 mb-2 lg:mb-0">Bootzooka</span>
          <span className="text-sm">
            application scaffolding tool by{' '}
            <a href="http://softwaremill.com" className="underline">
              SoftwareMill
            </a>
          </span>
        </div>
      </div>
      <div>
        <div className="flex items-center w-full gap-4 justify-center lg:justify-between">
          <a
            href="https://github.com/softwaremill/bootzooka"
            target="_blank"
            rel="noopener noreferrer"
          >
            <SiGithub className="w-7 h-7" />
          </a>
          {mutation.isLoading && (
            <span className="text-sm">Retrieving version...</span>
          )}
          {mutation.isSuccess && (
            <>
              <span className="text-sm overflow-hidden hidden lg:inline max-w-[180px] text-ellipsis">
                <Tooltip>
                  <TooltipTrigger asChild>
                    <span>Version: {mutation.data.buildSha}</span>
                  </TooltipTrigger>
                  <TooltipContent>
                    <span className="text-sm">{mutation.data.buildSha}</span>
                  </TooltipContent>
                </Tooltip>
              </span>
              <span className="lg:hidden mt-2">
                <Tooltip>
                  <TooltipTrigger>
                    <InfoIcon className="w-8 h-8" />
                  </TooltipTrigger>
                  <TooltipContent>
                    <span className="text-sm">
                      Version: {mutation.data.buildSha}
                    </span>
                  </TooltipContent>
                </Tooltip>
              </span>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
