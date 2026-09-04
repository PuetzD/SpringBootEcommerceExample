import CloseOutlinedIcon from '@mui/icons-material/CloseOutlined'
import MenuOutlinedIcon from '@mui/icons-material/MenuOutlined'
import RefreshOutlinedIcon from '@mui/icons-material/RefreshOutlined'

type AdminHeaderProps = {
  isDrawerOpen: boolean
  onToggle: () => void
}

export function AdminHeader({ isDrawerOpen, onToggle }: AdminHeaderProps) {
  return (
    <header className="navbar border-b border-base-300 bg-base-100/95 px-4 shadow-sm backdrop-blur">
      <div className="flex-1 items-center gap-3">
        <button
          type="button"
          className="btn btn-ghost btn-square lg:hidden"
          aria-label={isDrawerOpen ? 'Close navigation' : 'Open navigation'}
          aria-expanded={isDrawerOpen}
          aria-controls="admin-drawer"
          onClick={onToggle}
        >
          {isDrawerOpen ? (
            <CloseOutlinedIcon aria-hidden="true" fontSize="small" />
          ) : (
            <MenuOutlinedIcon aria-hidden="true" fontSize="small" />
          )}
        </button>

        <div className="flex items-center gap-3">
          <div className="avatar placeholder">
            <div className="bg-primary text-primary-content rounded-box h-10 w-10 text-sm font-bold">A</div>
          </div>

          <div>
            <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-base-content/60">ShopHappens</p>
            <h1 className="text-lg font-semibold leading-tight">Admin panel</h1>
          </div>
        </div>
      </div>

      <div className="flex-none gap-2">
        <button type="button" className="btn btn-ghost btn-sm" aria-label="Refresh data">
          <RefreshOutlinedIcon aria-hidden="true" fontSize="small" />
        </button>
        <button type="button" className="btn btn-primary btn-sm">
          New report
        </button>
      </div>
    </header>
  )
}
